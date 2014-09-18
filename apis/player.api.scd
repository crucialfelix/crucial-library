var
  register = IdentityDictionary.new,
  asuuid = { arg uuid;
    (uuid ?? {9999999999999.rand}).asSymbol;
  },
  get = { arg uuid;
    var p = register[uuid.asSymbol];
    if(p.isNil, {
      Error("Player not in registery:" + uuid.asCompileString).throw;
    });
    p
  },
  set = { arg uuid, player;
    register[uuid.asSymbol] = player;
  };

(
  // creation
  new: { arg reply, uuid, klass ... args;
    var
      player = klass.asSymbol.asClass.new(*args);
    uuid = asuuid.(uuid);
    set.(uuid, player);
    reply.value(uuid);
  },
  load: { arg reply, uuid, path;
    var player = AbstractPlayer.load(path);
    uuid = asuuid.(uuid);
    set.(uuid, player);
    reply.value(uuid);
  },

  // list
  listRegister: { arg reply;
    reply.value(register.keys.as(Array));
  },
  freeAll: { arg reply;
    register.keysValuesDo({ arg k, v;
      v.free;
    });
    register = ();
    reply.()
  },

  // instance
  free: { arg reply, uuid;
    register[uuid].free;
    register.removeAt(uuid);
    reply.value();
  },
  play: { arg reply, uuid, atTime;
    get.(uuid).play(atTime: atTime);
    reply.value();
  },
  stop: { arg reply, uuid, atTime;
    get.(uuid).stop(atTime: atTime);
    reply.value();
  },
  isPlaying: { arg reply, uuid;
    reply.value(get.(uuid).isPlaying);
  },
  info: { arg reply, uuid;
    var p = get.(uuid);
    reply.value((
      name: p.name,
      path: p.path
    ));
  },

  // not for remove calling.
  // this is for other apis to get a player object
  _get: { arg reply, uuid;
    reply.(get.(uuid));
  },
  _set: { arg reply, uuid, player;
    reply.(set.(uuid, player));
  },
  prGetOrSet: { arg reply, player;
    var uuid = register.findKeyForValue(player);
    if(uuid.isNil, {
      uuid = asuuid.(nil);
      set.value(uuid, player);
    });
    reply.value(uuid);
  }
);
