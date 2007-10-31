OSCClockReceiver
{
  var
  internalClock,
  syncResponder, 
  timeResponder, 
  state, 
  currentRemoteTime,
  currentSystemTime,
  nextSystemTime,
  firstTime,
  z,
  <>w,
  accumTime,
  <>verbose = false;
  
  *new
  {
	arg
	argW = 0.01;

	^super.new.init(argW);
  }
  
  init
  {
	arg
	argW;

	"[ClockReceiver]: Init...".postln;

	w = argW;

	internalClock = SystemClock;

	this.reset;

	timeResponder = OSCresponder.new(
	  nil,
	  "/clock_sync/time", 
	  {
		|time, resp, msg, addr| 
		//"[ClockReceiver]: action...".postln;
		this.timeCallback(time, resp, msg, addr)
	  }
	).add;
  }

  reset
  {
	"[ClockReceiver]: reset...".postln;

	// internalClock.clear;
	accumTime = 0;
	firstTime = true;
	z = 0.0;
	currentRemoteTime = 0;
	currentSystemTime = 0;
	nextSystemTime = 0;
	state = \stopped;
  }

  timeCallback {
	arg 
	time, 
	theResponder, 
	message, 
	addr; 

	var 
	remoteTime, 
	periodTime,
	d;


	//"[ClockReceiver]: timeCallback".postln;

	remoteTime = message[1].asFloat;
	periodTime = 1.0/message[2];

	if (firstTime,
	  {
		//"[ClockReceiver]: firstTime == true".postln;
		state = \running;
		accumTime = 0;
		firstTime = false;
		currentSystemTime = internalClock.seconds;
		nextSystemTime = currentSystemTime + periodTime;
		z = 0;
		currentRemoteTime = remoteTime;
		^nil;
	  },
	  {
		//"[ClockReceiver]: firstTime == false".postln;
		d = (internalClock.seconds) - nextSystemTime;
		z = z + (w * (d - z));
		currentSystemTime = nextSystemTime;
		nextSystemTime = nextSystemTime + z + periodTime;

		if (verbose == true,
		  {
			accumTime = accumTime + periodTime;
			if (accumTime > 1,
			  {
				("[ClockReceiver]: z: " ++ z ++ " d: " ++ d ++ " message: " ++ message).postln;
				accumTime = 0;
			  });
		  });

	  });

	if (currentRemoteTime > remoteTime,
	  {
		"[ClockReceiver]: Sync messages out of order. Resyncing...".postln;
		firstTime = true;
		this.reset;
		^nil;
	  });
	
	currentRemoteTime = remoteTime;
  }

  sched {
	arg delta, function;

	schedAbs([(this.seconds + delta), function]);
  }

  schedAbs {
	arg time, function;

	if (state != \running, {
	  "[ClockReceiver]: not RUNNING. Start clock sender on remote end..".postln;
	  ^nil;
	});
	internalClock.sched(time - this.seconds, {
	  arg system_time;
	  var ret;

	  ret = function.value(system_time);
	  if (ret.isKindOf(Number),

		{
		  this.schedAbs(time + ret, function);
		});

	  nil;
	});
  }

  clear {
	internalClock.clear;
  }

  seconds {
	if (state == \running,
	  {
		^(currentRemoteTime + (internalClock.seconds - currentSystemTime));
	  });

	^nil;
  }
}

