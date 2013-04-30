

+ SequenceableCollection {

	ilisp {
		var operator, subject, arguments;
		# operator ... arguments = this;
		if(operator.isString, {
			// accept keyword arguments
			if(arguments.size == 1 and: {arguments.first.isKindOf(Dictionary)}, {
				^operator.asInstr.papply(arguments.first.ilisp);
			},{
				^operator.asInstr.papply(arguments.collect(_.ilisp));
			})
		});
		subject = arguments.removeAt(0);
		if(subject.isSequenceableCollection, {
			subject = subject.ilisp;
		});
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

