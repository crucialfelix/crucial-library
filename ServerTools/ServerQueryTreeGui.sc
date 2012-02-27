

ServerQueryTreeGui : ObjectGui { // model is Server
	
	*makeWindow { arg server;
		var gui;
		server = server ? Server.default;
		gui = super.new(server);
		server.getQueryTree({ arg rootData;
			gui.gui(nil,nil,rootData)
		})
	}
	gui { arg layout,bounds,root;
		
		var server;
		server = model;

		server.getQueryTree({ arg root;
			
			var renderChild,indent = 0,w,f;
			var nodeWatcher,y=0,renderBox, layout;

			nodeWatcher = NodeWatcher.newFrom(server);

			if(layout.isNil,{
				w = Window("Server Node Tree",Rect(0,0,1220,820),scroll:true);
				f = CompositeView(w,Rect(0,0,1210,810));
				w.front;
			},{
				f = CompositeView(layout,bounds ?? {layout.bounds})
			});

			renderBox = { arg func;
				var box;
				box = f.flow(func,Rect(indent * 5,y,900,600));
				box.resizeToFit;
				box.background = Color.yellow(alpha:0.1);
				y = y + box.bounds.height + 4;
			};
			
			renderChild = { arg data;
				data.use({
					var node;
			
					if(~nodeType == Group,{
						node = nodeWatcher.nodes.at(~id) ?? {Group.basicNew(server,~id)};
	
						renderBox.value({ arg l;
							SimpleLabel(l,("Group(" ++ ~id ++ ")")).background_(ServerLogGui.colorForNodeID(~id) ).bold;
							//ToggleButton(l,"pause",{ arg way; node.run(way) },init:true);
							ActionButton(l,"free",{ node.free });
							Annotations.guiFindNode(~id,l);
						});
						
						indent = indent + 8;
						~children.do { arg child;
							renderChild.value(child);
						};
						indent = indent - 8;
						
					},{
						node = nodeWatcher.nodes.at(~id) ?? {Synth.basicNew(~defName,server,~id)};
						renderBox.value({ arg l;
							SimpleLabel(l,("Synth(" ++ ~id ++ ")")).background_(ServerLogGui.colorForNodeID(~id) ).bold;
							DefNameLabel(~defName,server,l);
							ActionButton(l,"trace",{
								node.trace;
							});
							//ToggleButton(l,"pause",{ arg way; node.run(way) },init:true);
							ActionButton(l,"free",{ node.free });
							l.startRow;
							ServerLog.guiMsgsForSynth(node,l);
							l.startRow;
							Annotations.guiFindNode(~id,l);
							~controls.keysValuesDo { arg k,v;
								l.startRow;
								ArgName(k,l,100);
								SimpleLabel(l,v,100);
							};
						})
					});
				});
			};
			renderChild.value(root);
		})
	}
}


BussesTool {

	var <>server;

	*new { arg server;
		^super.new.server_(server ? Server.default).gui
	}
	gui { arg layout,bounds;
		var resize = false;
		if(layout.isNil,{ layout = FlowView.new; resize=true });
		SimpleLabel( layout, "Audio Busses",width:685);
		server.audioBusAllocator.blocks.do({ |b|
			var listen,bus;
			listen = Patch({ In.ar( b.start, b.size ) });
			layout.startRow;
			ToggleButton( layout,"listen",{
				listen.play
			},{
				listen.stop
			});
			SimpleLabel( layout, b.start.asString + "(" ++ b.size.asString ++ ")",100 );

			Annotations.guiFindBus(b.start,b.size,layout);

			if(BusPool.notNil,{
				bus = BusPool.findBus(server,b.start);
				if(bus.notNil,{
					layout.flow({ |f|
						var ann;
						ann = BusPool.getAnnotations(bus);

						if(ann.notNil,{
							ann.keysValuesDo({ |client,name|
								f.startRow;
								InspectorLink(client,f);
								SimpleLabel(f,":"++name);
							});
						});
					})
				});
			});
			ActionButton(layout,"search ServerLog",{
				ServerLog.guiMsgsForBus(b.start,b.size)
			});
		});
		if(resize,{ layout.resizeToFit })
	}
}


