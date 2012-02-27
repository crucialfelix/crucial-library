
+ Object {
	die { arg ... culprits;
		"\n\nFATAL ERROR:\n".postln;
		culprits.do({ arg c; if(c.isString,{c.postln},{c.dump}) });
		this.dump;
		Error("FATAL ERROR").throw;
	}
	checkKind { arg shouldBeKindOf;
		if(this.isKindOf(shouldBeKindOf).not,{
			Error("Type check failed:" + this + "should be kind of:" + shouldBeKindOf).throw;
		})
	}
}

