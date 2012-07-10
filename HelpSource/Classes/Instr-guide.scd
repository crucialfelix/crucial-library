


On Tue, May 22, 2012 at 5:04 PM, Monsieur <grirgz@gmail.com> wrote:
Hi,

I'm tired of rewriting each time the same code for my synthdefs, so i'm looking for a way to re-use code. My first idea was to use the Instr class, but despite its wonderful features, i find it not adapted at all to my needs:
- I can't use named arguments


actually you can:

Instr("saw").asSynthDef( (amp: 0.3) )

they are just functions, so you can use them anywhere:

SynthDef("thingy",{

    // call it like a subroutine
    Instr("saw").value( (amp: 0.3) )

}).add; 


// this is the equivalent of wrap
Instr("saw") <>> Instr("rlpf")

[see below for more]

 
- Working with patterns is painful

Do you mean using instr with patterns ?

its the same as for a SynthDef

sd = Instr("saw").add
sd.defName
// saw#1WjOV

but you can also supply arguments (to eliminate them, to satisfy them or to vary the architecture with quantity etc)

sd = Instr("saw").add( (amp: -10.dbamp) )
sd.defName
//saw#9EFru

I would like to add this feature:
sd = Instr("saw").add( (amp: \ir) )

now it would have an ir input


see reverb example at the bottom (long) for using quantity as a compile time parameter.

you can also simply play them as synths

Instr("saw",{ arg freq=440,amp=0.9,dur=0.25;
	EnvGen.kr(Env.sine(dur:dur),doneAction:2) * Saw.ar(freq,amp)
})

Instr("saw").tail( s, (freq:  rrand(100,400) ) )

and even use them directly:

Pbind(
 \type, \instr,
  \instr, "saw",
   \degree, Pbrown(1,20,1)
   ).play

Pbind(
 \type, \instr,
  \instr, "saw",
   \degree, Pbrown(1,20,1),
   \dur, Pseq([0.25,1.0,0.75],inf)
   ).play
 

note that instr.add creates a SynthDef and adds that, so its for normal pattern usage.  nothing is different than normal SynthDefs, it just creates the SynthDef using the function

if you use 'type','instr' then specify the instr name and you do not even need to add it. it will create new synth defs and send them on demand if the synth def architecture has not yet been seen/sent.  so it would be inefficient if you keep tossing it random quantities/envs etc.  but it does know to use .ir inputs for all the control ones



 
- I need a way to automatically add arguments to the higher synthdef (see below)

What I want is something like this:

(
    ISynthDef(\saw, { arg freq=200,amp=0.5;
        LFSaw.ar(freq,0.0, amp);
    }).add;
   
    ISynthDef(\rlpf, { arg in,freq=200,rq=0.1;
        RLPF.ar(in, freq, rq);
    }).add;
   
    ISyntDef(\ppp, { arg freq=200;
        var ou;
        ou = ISynthDef(\saw).ar(freq:freq, amp:0.5);
        ISynthDef(\rlpf).ar(ou, freq)) ! 2
    }).add;

Instr("ppp",{ arg freq=200;
   var ou;
  ou = Instr("saw").ar( (freq: freq, amp: 0.5 ) );
  Instr("rlpf").ar( ou, freq)) ! 2
}).add

that's it exactly as you want it :)

additionally of course you can store the Instr in pages and page of collected instr and be able to load them dynamically.


In \ppp, the rq argument from \rlpf is automatically added to argument of \ppp.

ah, that might be trickier to do in a readable fashion.  I prefer simplicity

   Instr(\saw, { arg freq=200,amp=0.5;
        LFSaw.ar(freq,0.0, amp);
    });
    
    Instr(\rlpf, { arg in,freq=200,rq=0.1;
        RLPF.ar(in, freq, rq);
    });
    

i = Instr("saw") <>> Instr("rlpf")

<>>.saw|@0|rlpf

i.gui

has arguments:
  freq
  amp
  freq2  [because it would be a duplicate arg name]
  rq
  
// ready for patterns
i.add



if you wanted only the one freq I would just do this:

Instr("combo",{ arg freq,rq,amp;

	ou = Instr("saw").value( freq, amp );
	Instr("rlpf").value( ou, freq, rq)

})


a <>> b assumes to inject the output of a into the first input of b

but you can also create a CompositeInstr yourself and specify any input of b


Instr("reverbs.reverberator3",{
	arg audio, //stereo or mono
		revBalance=0.0,// 0..1

		revTime=4,
		taps=10,
		combs=6,
		allpasses=4,
		tapsMin=0.01,
		tapsMax=0.07,
		combsMin=0.3,
		combsMax=0.5,
		allpassMin=0.01,
		allpassMax=0.05,
		allpassDecay=1.0;

	var 	tapsOut,out,tapsLevelMax,combinput,combLevelMax;

	if(taps > 0,{
		tapsLevelMax = taps.reciprocal;

		tapsOut = 
			Mix.fill(taps,{
				var delays;
				delays = rrand(tapsMin,tapsMax);
				DelayN.ar(audio,delays,delays,tapsLevelMax.rand)
			}) + audio
	},{
		tapsOut = audio;
	});

	// 0 is permissable
	if(combs == 0,{
		out = tapsOut;
	},{
		combs.do({ arg ci;
			var times;
			times = rrand(combsMin,combsMax);
			if(combinput.isNil, { //first time thru
				combLevelMax = combs.reciprocal; // first time, else possible div by 0
				combinput = tapsOut;
				out = CombC.ar(  
							combinput,times,times,
							revTime,combLevelMax.rand)
			},{
				out = out + CombC.ar( 
							combinput,times,times,
							revTime,combLevelMax.rand)
			})
		});
	});

	out = out * (Amplitude.kr(Mono(out)) > -160.dbamp);

	// 0 is permissable
	allpasses.do({ arg i;
		var times;
		times = 	[rrand(allpassMin,allpassMax),rrand(allpassMin,allpassMax)];

 		out = AllpassC.ar(
				out,
				// first time thru its possibly still mono, 
 				// then it expands to stereo and each subsequent is paired up to that expansion

				times,
				times,
				allpassDecay)
	});
	 
	out = out * (Amplitude.kr(Mono(out)) > -160.dbamp);
	 
	XFader.ar(audio,out,revBalance);

},[
	'audio',
	\unipolar,
	[0,20],
	StaticIntegerSpec(0,20),
	StaticIntegerSpec(0,20),
	StaticIntegerSpec(0,20),
	StaticSpec(0.0001,0.5),
	StaticSpec(0.0001,1.0),
	StaticSpec(0.0001,0.5),
	StaticSpec(0.0001,1.0),
	StaticSpec(0.0001,0.5),
	StaticSpec(0.0001,1.0),
	ControlSpec(0,10.0)
],
	\stereo 
);

