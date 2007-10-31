/**
	A TempoClock-like clock that uses another clock 
	(e.g. OSCClockReceiver) as timing source..
*/
SlaveTempoClock
{
	var
	clock,
	startingTime,
	<beatsPerBar = 4.0,
	<baseBarBeat = 0.0,
	<baseBar = 0.0,
	<>tempo;


	*new
	{
		arg
		argClock = SystemClock,
		argTempo = 1,
		argStartingTime = argClock.seconds;

		// "[SlaveTempoClock]: new()".postln;

		^super.new.init(argClock, argTempo, argStartingTime);
	}
	
	init
	{
		arg
		argClock,
		argTempo,
		argStartingTime;
		
		// "[SlaveTempoClock]: init()".postln;

		tempo = argTempo;
		clock = argClock;
		startingTime = argStartingTime;
	}

	sched
	{
		arg
		delta,
		function;
	
		// "[SlaveTempoClock]: sched()".postln;

		this.schedAbs(this.beats + delta, function);
	}


	schedAbs
	{
		arg
		beat,
		function;

		// "[SlaveTempoClock]: schedAbs()".postln;

		clock.schedAbs
		(
			this.beats2secs(beat),
			{
				arg
				clockTime;

				var
				ret;

				ret = function.value(this.secs2beats(clockTime));

				if 
				(
					ret.isKindOf(Number),
					{
						this.schedAbs(beat + ret, function)
					}
				)
			}
		)
	}
	
	clear
	{
		// "[SlaveTempoClock]: clear()".postln;
		clock.clear
	}

	beats2secs
	{
		arg
		beats;

		// "[SlaveTempoClock]: beats2secs()".postln;

		^((beats/tempo) + startingTime);
	}

	secs2beats
	{
		arg
		secs;

		// "[SlaveTempoClock]: secs2beats()".postln;

		^((secs - startingTime)*tempo);
	}

	beatDur
	{
		// "[SlaveTempoClock]: beatDur()".postln;
		^(1.0/tempo);
	}
	
	beats
	{
		// "[SlaveTempoClock]: beats()".postln;

		^((clock.seconds - startingTime)*tempo);
	}

	nextTimeOnGrid 
	{ 
		arg 
		quant = 1, 
		phase = 0;

		var 
		offset;

		if (quant < 0) { quant = beatsPerBar * quant.neg };
		offset = baseBarBeat + phase;
		^roundUp(this.beats - offset, quant) + offset;
	}

	play 
	{ 
		arg 
		task, 
		quant = 1; 
		
		this.schedAbs(quant.nextTimeOnGrid(this), task) 
	}
}