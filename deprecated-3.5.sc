

MultiPageLayout : PageLayout { 

	*new { arg ... args;
		this.deprecated(thisMethod);
		^PageLayout(*args)
	}
}


// move these to main pending-deprecated-3.6.sc

+ PageLayout {
	
	layRight { arg w,h; 
		this.deprecated(thisMethod)
		^Rect(0,0,w,h) 
	}
}


// these were a bad idea since bounds and layout could easily be swapped
// and this would hide the error by treating the point/rect as a layout

+ Point {
	asPageLayout {
		^PageLayout("",this.asRect ).front
	}
}

+ Rect {
	asPageLayout {
		^PageLayout("",this ).front
	}
}


