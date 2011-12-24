

+ Object {

	insp { arg  ... args;
		Insp(this,args);
	}
	// gui into the Insp tabbed browser
	ginsp { arg  ... args;
		Insp(this,args,true);
	}
}

+ Class {
	guiClass { ^ClassGui }
}

+ Method {
	guiClass { ^MethodGui }
}

