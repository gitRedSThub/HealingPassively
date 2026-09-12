# HealingPassively

A small Paper plugin for making mobs heal themselves over time.

## How it works

By default:

* It checks **64 blocks** around each player.
* It checks every **10 seconds**.
* It heals for **1 HP** each time.
* It only heals mobs on the whitelist.

So if your cow has 12 HP, it'll get healed when the next check happens.

If two players are standing next to the same cow, it doesn't get healed twice. The plugin knows it's the same cow.

## Configuration

The config is pretty small.

```yaml
distance: 64
timer: 10
heal-amount: 1

in-whitelist: true

white-list:
  - armadillo
  - bat
  - cat
  - chicken
  - cow
  - donkey
  - fox
  - horse
  - mooshroom
  - mule
  - ocelot
  - parrot
  - pig
  - rabbit
  - sheep
  - sniffer
  - snow_golem
  - villager
  - wandering_trader
  - axolotl
  - camel
  - cod
  - frog
  - glow_squid
  - salmon
  - squid
  - tadpole
  - tropical_fish
  - turtle
  - strider
```

### `distance`

How far the plugin looks around players.

```yaml
distance: 64
```

64 means 64 blocks.

The distance also counts up and down, so it's not just a flat circle on the ground.

### `timer`

How long the plugin waits between checks.

```yaml
timer: 10
```

It isn't constantly running every tick.

### `heal-amount`

How much health gets added.

```yaml
heal-amount: 1
```

It also won't push something past its maximum health.

Obviously.

### `in-whitelist`

This decides whether the whitelist matters.

```yaml
in-whitelist: true
```

Only the mobs in `white-list` will be healed.

Set it to:

```yaml
in-whitelist: false
```

and the list is ignored.

That's useful if you just want nearby living entities to heal without maintaining a list.

## Commands

### Help

```text
/healingpassively help
```

Shows the available commands.

### Set the distance

```text
/healingpassively set distance <amount>
```

### Set the timer

```text
/healingpassively set timer <seconds>
```

### Turn the whitelist on or off

```text
/healingpassively set if-in-whitelist <true|false>
```

### Add something to the whitelist

```text
/healingpassively whitelist add <entity>
```

### Remove something

```text
/healingpassively whitelist remove <entity>
```

### See the list

```text
/healingpassively whitelist list
```

### See the current settings

```text
/healingpassively get info
```

This shows the current distance, timer, healing amount, whitelist setting, and whitelist.

### Reload

```text
/healingpassively reload
```

## Requirements

* Minecraft **1.21.11**
* **Paper**
* **Java 21+**

---

Made by **_RedST**
