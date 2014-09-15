
VertLayout : Decorator {

	var <>bounds, <>margin, <>gap;
	var <>current;
	var <>owner;

	*new { arg bounds, margin, gap;
		^super.newCopyArgs(bounds, margin, gap).init
	}
	init {
		gap = gap ? Point(4, 4);
		margin = margin ? Point(4, 4);
		this.reset;
	}
	clear { this.reset; }
	reset {
		current = bounds.top + margin.y;
	}
	place { arg view;
		var height;
		height = view.bounds.height;
		view.bounds = Rect(margin.x, current, bounds.width - margin.x, height);
		current = current + height + margin.y;
	}
	remove { }
	innerBounds {
		^bounds.insetBy(margin.x * 2, margin.y * 2)
	}
}


HorzLayout : VertLayout {

	reset {
		current = margin.y;
	}
	place { arg view;
		var width;
		width = view.bounds.width;
		view.bounds = Rect(current + margin.x, bounds.top, width, bounds.height - margin.y);
		current = current + width + margin.x;
	}
}
