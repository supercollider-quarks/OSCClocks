
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
	A timeout. If for longer than this time no more /time
	messages arrive, we quit waiting and assume sender to be 
	dead
  */
  timeout,

  /**
	the masterclock we send over the net and wich
	we derive our own timing from
  */
  clock,

  /**
	The target addresses (a Collection of NetAddr).
  */
  <>targets;

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
	  Default timeut
	*/
	argTimeout = 5;

	^super.new.init(argClock, argFreq, argTimeout);
  }
 
  init
  {
	arg 
	argClock,
	argFreq,
	argTimeout;

	"[ClockSender]: init()".postln;

	timeout = argTimeout;
	freq = argFreq;
	clock = argClock;
  }

  *initClass
  {
	"[ClockSender]: initClass()".postln;

	OSCClockSender.default = OSCClockSender.new(SystemClock);
  }

  /**
	Sets the clock to use. Stops all the sending of clock times..
	makes sclang segfault during compile - ugh..
  */
  setClock
  {
	arg argClock;

	// stop();
	// this.clock = clock;
  }

  /**
	Start sending
  */
  start
  {
	"[ClockSender]: starting to send clock signal".postln;
	targets.do
	({
	  arg i;
	  i.sendMsg("/clock_sync/start");
	});

	clock.schedAbs
	(
	  clock.seconds.ceil, 
	  {
		targets.do
		({
		  arg i;
		  i.sendMsg("/clock_sync/time", clock.seconds.asString, freq);
		});
		// this.callback(clock); 

		// we want to get called once every 1/freq secs.
		(1.0/freq);
	  }
	);
  }

  /**
	Stop sending
  */
  stop
  {
	"[ClockSender]: stopping..".postln;

	targets.do
	({
	  arg i;
	  i.sendMsg("/clock_sync/stop");
	});

	this.clock.clear;
  }
}