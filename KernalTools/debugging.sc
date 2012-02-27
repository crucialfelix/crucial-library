
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


+ Class {

	classesReferenced {
		var package = this.package;
		var refklasses = IdentitySet.new;
		this.methods.do { arg meth;
			if(meth.package == package,{
				refklasses.addAll(meth.classesReferenced)
			})
		};
		^refklasses
	}
	referencesTo { // slow !
		^Class.allClasses.select({ arg class;
			class.classesReferenced.includes(this)
		})
	}
}

+ Method {

	classesReferenced {
		var selectors,refklasses;
		refklasses = [];
		selectors = this.selectors;
		if(selectors.notNil,{
			refklasses = selectors.asArray.select({ arg lit;
				var klass;
				if(lit.isKindOf(Symbol) and: { lit.isClassName },{
					klass = lit.asClass;
					klass.notNil
				},{
					false
				})
			});
			^refklasses.as(IdentitySet).collect(_.asClass)
		});
		^refklasses
	}
}
