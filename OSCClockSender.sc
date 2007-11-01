
/**
  We don't inherit any particular clock for flexibility reasons.. This way 
  we can "hook" onto any clock that allows scheduling
*/
OSCClockSender
{
  classvar
  /**
	One instance of this class is created at system startup using the 
	default values (SystemClock, 50).
  */
  <>default;
  
  var
  /**
	The frequency we send with
  */
  freq,

  /**
	state [running/stopped]
  */
  state = \stopped,

  /**
	the masterclock we send over the net and wich
	we derive our own timing from
  */
  clock,

  /**
	A prefix to the paths used in the OSC messages.
  */
  <>path,

  /**
	The target addresses (a Collection of NetAddr).
  */
  <>targets,

  /**
	some verbose output to tell the user we be alive ;)
  */
  <>verbose,
  <>verboseTime = 5,
  accumTime;

  *new
  {
	arg 
	/**
	  The clock to send time off via OSC
	*/
	argClock = SystemClock,
	/**
	  The frequency of the messages
	*/
	argFreq = 50,
	/**
	  The path to send to
	*/
	argPath = "/OSCClocks";

	^super.new.init(argClock, argFreq, argPath);
  }
 
  init
  {
	arg 
	argClock,
	argFreq,
	argPath;

	// "[ClockSender]: init()".postln;

	path = argPath;
	freq = argFreq;
	clock = argClock;
  }

  *initClass
  {
	// "[ClockSender]: initClass()".postln;

	OSCClockSender.default = OSCClockSender.new(SystemClock);
  }

  /**
	Sets the clock to use. Stops all the sending of clock times..
  */
  setClock
  {
	arg argClock;

	this.stop;
	this.clock = clock;
  }

  /**
	Start sending
  */
  start
  {
	if (state == \running, 
	  {
		"[OSCClockSender]: already running....".postln;
		^this;
	  }
	);

	"[OSCClockSender]: starting to send clock signal".postln;

	accumTime = 0;

	state = \running;

	targets.do
	({
	  arg i;
	  i.sendMsg(path ++ "/start");
	});

	clock.schedAbs
	(
	  clock.seconds.ceil, 
	  {
		accumTime = accumTime + (1.0/freq);
		if ((accumTime > verboseTime).and(verbose == true),
		  {
			accumTime = accumTime % verboseTime;
			("[OSCClockSender]: Sending time: " ++ clock.seconds).postln;
		  }
		);

		if (state == \stopped,
		  {
			targets.do(
			  {
				arg i;
				i.sendMsg(path ++ "/stop");
			  }
			);

			// stop being called..
			nil;
		  },
		  {
			targets.do(
			  {
				arg i;
				i.sendMsg(path ++ "/time", clock.seconds.asString, freq);
			  }
			);
			
			// we want to get called once every 1/freq secs.
			(1.0/freq);
		  }
		);
	  }
	);
  }
	 

  /**
	Stop sending
  */
  stop
  {
	if (verbose == true,
	  {
		"[OSCClockSender]: stopping..".postln;
	  }
	);

	state = \stopped;
  }
}