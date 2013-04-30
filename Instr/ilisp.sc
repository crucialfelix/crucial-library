

+ SequenceableCollection {

	ilisp {
		var operator, subject, arguments, imethod='papply';
		# operator ... arguments = this;
		if(operator.isString, {
			// a #macro just values the function
			if(operator.first == $#, {
				imethod = 'valueArray';
			});
			// accept keyword arguments
			if(arguments.size == 1 and: {arguments.first.isKindOf(Dictionary)}, {
				^operator.asInstr.perform(imethod, arguments.first.ilisp);
			},{
				^operator.asInstr.perform(imethod, arguments.collect(_.ilisp));
			})
		});
		subject = arguments.removeAt(0).ilisp;
		^subject.performList(operator, arguments.collect(_.ilisp));
	}
}

+ Object {

	ilisp {
		^this.value
	}
}

+ Dictionary {

	ilisp {
		^this.collect({ arg elem, key; elem.ilisp });
	}
}

