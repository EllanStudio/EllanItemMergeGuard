# EllanItemMergeGuard

Prevents selected dropped items from merging through Paper's `ItemMergeEvent`.

This only affects item entities in the world. It does not change stack merging
inside player inventories, chests, hoppers, or other container inventories.

Materials with a maximum stack size of 1 do not produce merge events, but they
may remain in the configuration for explicit policy and documentation.

## Commands

- `/ellanitemmerge reload`
- `/ellanitemmerge status`

Permission: `ellanitemmerge.admin`

## Compatibility

- Paper API compile target: `26.3.build.157-beta`.
- The plugin descriptor keeps `api-version: 1.21`; live server acceptance remains required.
