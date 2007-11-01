OSCClockReceiver
{
  var
  internalClock,
  timeResponder, 
  state,
  path,

  currentRemoteTime,
  currentSystemTime,
  nextSystemTime,
  firstTime,
  z,
  <>w,

  accumTime,
  <>verbose = false,
  <>verboseTime = 5;
  
  *new
  {
	arg
	argW = 0.01,
	argPath = "/OSCClocks";

	^super.new.init(argW, argPath);
  }
  
  init
  {
	arg
	argW,
	argPath;

	// "[ClockReceiver]: Init...".postln;

	w = argW;
	path = argPath;

	internalClock = SystemClock;

	this.reset;

	timeResponder = OSCresponder.new(
	  nil,
	  path ++ "/time", 
	  {
		arg
		time, 
		resp, 
		msg, 
		addr;
 
		//"[OSCClockReceiver]: action...".postln;
		this.timeCallback(time, resp, msg, addr)
	  }
	).add;
  }

  reset
  {
	if (verbose == true,
	  {
		"[OSCClockReceiver]: resetting...".postln;
	  }
	);

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


	//"[OSCClockReceiver]: timeCallback".postln;

	remoteTime = message[1].asFloat;
	periodTime = 1.0/message[2];

	if (firstTime,
	  {
		if (verbose == true,
		  {
			"[OSCClockReceiver]: Receiving time messages...".postln;
		  }
		);

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
		//"[OSCClockReceiver]: firstTime == false".postln;
		d = (internalClock.seconds) - nextSystemTime;
		z = z + (w * (d - z));
		currentSystemTime = nextSystemTime;
		nextSystemTime = nextSystemTime + z + periodTime;

		accumTime = accumTime + periodTime;
		if ((accumTime > verboseTime).and(verbose == true),
		  {
			("[OSCClockReceiver]: z: " ++ z ++ " d: " ++ d ++ " message: " ++ message).postln;
			accumTime = 0;
		  }
		);
	  }
	);
	
	
	if (currentRemoteTime > remoteTime,
	  {
		if (verbose == true,
		  {
			"[OSCClockReceiver]: Sync messages out of order. Resyncing...".postln;
		  }
		);

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
	  "[OSCClockReceiver]: not RUNNING. Start clock sender on remote end..".postln;
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

