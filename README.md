# Tiered

Tiered is a Fabric mod inspired by [Quality Tools](https://www.curseforge.com/minecraft/mc-mods/quality-tools). Every tool you make will have a special modifier, as seen below:

<img src="resources/legendary_chestplate.png" width="400">

### Installation
Tiered is a mod built for the [Fabric Loader](https://fabricmc.net/). It requires [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api), [AutoTag](https://www.curseforge.com/minecraft/mc-mods/autotag) and [LibZ](https://www.curseforge.com/minecraft/mc-mods/libz) to be installed separately; all other dependencies are installed with the mod.

### Customization

Tiered is entirely data-driven, which means you can add, modify, and remove modifiers as you see fit. The base path for modifiers is `data/modid/item_attributes`, and tiered modifiers are stored under the modid of tiered. Here's an example modifier called "Hasteful," which grants more dig speed when any of the valid tools are held:
```json
{
  "id": "tiered:hasteful",
  "verifiers": [
    {
      "tag": "minecraft:pickaxes"
    },
    {
      "tag": "minecraft:shovels"
    },
    {
      "tag": "minecraft:axes"
    }
  ],
  "weight": 10,
  "style": {
    "color": "GREEN"
  },
  "attributes": [
    {
      "type": "generic.dig_speed",
      "modifier": {
        "name": "tiered:hasteful",
        "operation": "ADD_MULTIPLIED_TOTAL",
        "amount": 0.10
      },
      "optional_equipment_slots": [
        "MAINHAND"
      ]
    }
  ]
}
```

#### Attributes

Tiered currently provides 4 custom attributes: Dig Speed, Crit chance, Durability and Range Attack Damage. Dig Speed increases the speed of your block breaking (think: haste), Crit Chance offers a random chance to crit when using a tool and Durability increases, who would have thought it, the durability of an item.

Vanilla types:
- "generic.armor"
- "generic.armor_toughness"
- "generic.attack_damage"
- "generic.attack_knockback"
- "generic.attack_speed"
- "player.block_break_speed"
- "player.block_interaction_range"
- "generic.burning_time"
- "generic.explosion_knockback_resistance"
- "player.entity_interaction_range"
- "generic.fall_damage_multiplier"
- "generic.flying_speed"
- "generic.follow_range"
- "generic.gravity"
- "generic.jump_strength"
- "generic.knockback_resistance"
- "generic.luck"
- "generic.max_absorption"
- "generic.max_health"
- "player.mining_efficiency"
- "generic.movement_efficiency"
- "generic.movement_speed"
- "generic.oxygen_bonus"
- "generic.safe_fall_distance"
- "generic.scale"
- "player.sneaking_speed"
- "zombie.spawn_reinforcements"
- "generic.step_height"
- "player.submerged_mining_speed"
- "player.sweeping_damage_ratio"
- "generic.water_movement_efficiency"

Tiered Types:
- "tiered:generic.dig_speed"
- "tiered:generic.dig_speed"
- "tiered:generic.crit_chance"
- "tiered:generic.range_attack_damage"

#### Verifiers

A verifier (specified in the "verifiers" array of your modifier json file) defines whether or not a given tag or tool is valid for the modifier. 

A specific item ID can be specified with:
```json
"id": "minecraft:apple"
```

and a tag can be specified with:
```json
"tag": "minecraft:head_armor"
```

Tiered doesn't provide tags.  
Example tags in vanilla minecraft: `minecraft:pickaxes`, `minecraft:axes`, `minecraft:shovels`,`minecraft:hoes`, `minecraft:swords` and several more.  
Item tags can be found on the [wiki](https://minecraft.wiki/w/Tag#Item_tags_2).

#### Weight

The weight determines the commonness of the tier. Higher weights increase the chance of being applied on the item and vice versa.

#### Tooltip
Since V1.2, custom tooltip borders can get set via a resource pack.
- The border texture has to be in the `assets\tiered\textures\gui` folder.
- The file has to be a json file and put inside the `assets\tiered\tooltips` folder.
- The `background_gradient` can also get set.
- The gradients has to be hex code, check transparency here: [https://gist.github.com/lopspower/03fb1cc0ac9f32ef38f4](https://gist.github.com/lopspower/03fb1cc0ac9f32ef38f4)
- Check out the default datapack under `src\main\resources\assets\tiered\tooltips`.

Example:
```json
{ 
    "tooltips": [
        {
            "index": 0,
            "start_border_gradient": "FFBABABA",
            "end_border_gradient": "FF565656",
            "texture": "tiered_borders",
            "decider": [
                "set_the_id_here",
                "tiered:common_armor"
            ]
        }
    ]
}
```

#### Reforge

Reforging items to get other tiers can be done at the anvil. There is a slot which is called "base" on the left and a slot called "ADD_VALUE" on the right.
The ADD_VALUE slot can only contain items which are stated in the `tiered:REFORGE_ADDITION` item tag. The base slot can contain the reforging item material item if existent, otherwise it can only contain `tiered:reforge_base_item` tag items. The base slot item can get changed via datapack, an example can be found below and has to get put in the `tiered:reforge_items` folder.  
The `tiered:modifier_restricted` item tag can be used to restrict item modifiers including reforging.

```json
{
  "items": [
    "minecraft:bow"
  ],
  "base": [
    "minecraft:string"
  ]
}
```

### License
Tiered is licensed under MIT. You are free to use the code inside this repo as you want.
