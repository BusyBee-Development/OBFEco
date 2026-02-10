# Obfeco

**Obfeco** is a modern, lightweight, multi-currency economy plugin for Minecraft servers. It is designed to be efficient and flexible, supporting multiple currencies and integrating with popular plugins.

## Features

*   **Multi-Currency Support:** Create and manage multiple custom currencies.
*   **Lightweight:** Designed for performance.
*   **Modern:** Built for Minecraft 1.20+.
*   **Integration:** Soft dependencies on **Vault** and **PlaceholderAPI** for broad compatibility.
*   **Management GUI:** Easy-to-use GUI for managing currencies.
*   **Leaderboards:** View top balances.
*   **Admin Tools:** Comprehensive commands for creating, deleting, giving, taking, setting, and resetting balances.
*   **Migration:** Conversion tools from other economy plugins.

## Installation

1.  Download the latest `Obfeco.jar`.
2.  Place the jar file in your server's `plugins` folder.
3.  (Optional) Install [Vault](https://www.spigotmc.org/resources/vault.34315/) and [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for full functionality.
4.  Restart your server.

## Commands

The main command is `/obfeco`, with aliases `/eco` and `/economy`.

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/obfeco` | Main command | `obfeco.use` |
| `/obfeco balance [player]` | Check balance | `obfeco.balance` / `obfeco.balance.others` |
| `/obfeco pay <player> <amount>` | Pay another player | `obfeco.pay` |
| `/obfeco top` | View leaderboards | `obfeco.top` |
| `/obfeco create <name>` | Create a new currency | `obfeco.create` |
| `/obfeco delete <name>` | Delete a currency | `obfeco.delete` |
| `/obfeco give <player> <amount>` | Give currency to a player | `obfeco.give` |
| `/obfeco take <player> <amount>` | Take currency from a player | `obfeco.take` |
| `/obfeco set <player> <amount>` | Set a player's balance | `obfeco.set` |
| `/obfeco reset <player>` | Reset a player's data | `obfeco.reset` |
| `/obfeco convert` | Convert data from other plugins | `obfeco.convert` |
| `/obfeco reload` | Reload configuration | `obfeco.reload` |
| `/obfeco manage` | Open currency management GUI | `obfeco.manage` |

## Permissions

### User Permissions
*   `obfeco.use`: Basic command access (Default: true)
*   `obfeco.balance`: Check own balance (Default: true)
*   `obfeco.pay`: Pay other players (Default: true)
*   `obfeco.top`: View leaderboards (Default: true)

### Admin Permissions
*   `obfeco.balance.others`: Check other players' balance (Default: op)
*   `obfeco.admin`: Grants all admin sub-permissions (Default: op)
    *   `obfeco.create`
    *   `obfeco.delete`
    *   `obfeco.give`
    *   `obfeco.take`
    *   `obfeco.set`
    *   `obfeco.reset`
    *   `obfeco.convert`
    *   `obfeco.reload`
    *   `obfeco.manage`

## Support

For more information, visit the [BusyBee Discord](https://discord.gg/abdm29q7af).
