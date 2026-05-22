# design.pen Extract

Source: `design/design.pen`

This is a read-only extraction of the current Pencil design data. It is not a generated design token contract yet.

## File Structure

- Version: `2.11`
- Top-level frames:
  - `Frame` (`bi8Au`): 800 x 600, white placeholder
  - `Design System` (`EdtiN`): reusable components and examples
  - `Design System — Reference` (`Ory4O`): reference documentation inside the pen file
  - `App Screens` (`LKBGQ`): screen mockups

## Variables

### Colors

| Name | Value |
|---|---:|
| `color-background` | `#FFFFFF` |
| `color-danger` | `#CC0000` |
| `color-divider` | `#E0E0E0` |
| `color-on-primary` | `#FFFFFF` |
| `color-on-surface` | `#111111` |
| `color-primary` | `#1A6FCC` |
| `color-secondary-text` | `#555555` |
| `color-status-active` | `#1A8A1A` |
| `color-status-inactive` | `#888888` |
| `color-surface` | `#FFFFFF` |
| `danger-100` | `#FFE8E8` |
| `danger-500` | `#CC0000` |
| `danger-700` | `#990000` |
| `neutral-000` | `#FFFFFF` |
| `neutral-050` | `#F8F8F8` |
| `neutral-100` | `#F0F0F0` |
| `neutral-200` | `#E0E0E0` |
| `neutral-400` | `#AAAAAA` |
| `neutral-600` | `#666666` |
| `neutral-800` | `#333333` |
| `neutral-900` | `#1A1A1A` |
| `neutral-950` | `#111111` |
| `primary-100` | `#E8F0FF` |
| `primary-300` | `#7AAAF5` |
| `primary-500` | `#1A6FCC` |
| `primary-700` | `#0F4A99` |
| `primary-900` | `#062860` |
| `success-100` | `#E6F5E6` |
| `success-500` | `#1A8A1A` |
| `success-700` | `#116611` |

### Typography Numbers

| Name | Value |
|---|---:|
| `font-size-body1-guardian` | `16` |
| `font-size-body1-senior` | `20` |
| `font-size-body2-guardian` | `14` |
| `font-size-body2-senior` | `17` |
| `font-size-button-guardian` | `14` |
| `font-size-button-senior` | `22` |
| `font-size-caption-guardian` | `12` |
| `font-size-caption-senior` | `15` |
| `font-size-code` | `40` |
| `font-size-display` | `60` |
| `font-size-h1-guardian` | `22` |
| `font-size-h1-senior` | `28` |
| `font-size-h2-guardian` | `18` |
| `font-size-h2-senior` | `24` |

### Radius

| Name | Value |
|---|---:|
| `radius-sm` | `8` |
| `radius-md` | `12` |
| `radius-lg` | `16` |
| `radius-xl` | `24` |
| `radius-full` | `999` |

### Spacing

| Name | Value |
|---|---:|
| `space-2` | `2` |
| `space-4` | `4` |
| `space-8` | `8` |
| `space-12` | `12` |
| `space-16` | `16` |
| `space-24` | `24` |
| `space-32` | `32` |
| `space-48` | `48` |

## Reusable Components

### Button/Primary/Senior

- ID: `eT7Hs`
- Height: `72`
- Fill: `$primary-500` (`#1A6FCC`)
- Radius: `$radius-md` (`12`)
- Padding: `[32, 0]`
- Align: center / center
- Label: Inter `22`, weight `700`, fill `$neutral-000` (`#FFFFFF`)

### Button/Primary/Guardian

- ID: `jLo6S`
- Height: `52`
- Fill: `$primary-500` (`#1A6FCC`)
- Radius: `$radius-md` (`12`)
- Padding: `[24, 0]`
- Align: center / center
- Label: Inter `14`, weight `500`, fill `$neutral-000` (`#FFFFFF`)

### Button/Secondary/Senior

- ID: `v9qau`
- Height: `72`
- Radius: `$radius-md` (`12`)
- Padding: `[32, 0]`
- Stroke: `$primary-500` (`#1A6FCC`), inside, `1.5`
- Label: Inter `22`, weight `700`, fill `$primary-500`

### Button/Secondary/Guardian

- ID: `KQfoV`
- Height: `48`
- Radius: `$radius-md` (`12`)
- Padding: `[24, 0]`
- Stroke: `$primary-500` (`#1A6FCC`), inside, `1.5`
- Label: Inter `14`, weight `500`, fill `$primary-500`

### Button/Text

- ID: `e5f13l`
- Padding: `[8, 4]`
- Label: Inter `16`, fill `$primary-500`

### Button/Danger/Senior

- ID: `l0zu2x`
- Width: `fill_container`
- Height: `72`
- Fill: `$danger-500` (`#CC0000`)
- Radius: `$radius-md` (`12`)
- Padding: `[32, 0]`
- Label: Inter `22`, weight `700`, fill `$neutral-000`

### TextField/Default

- ID: `ehwGH`
- Width: `200`
- Vertical gap: `4`
- Label: Inter `12`, fill `$neutral-600`
- Box height: `56`
- Box radius: `$radius-sm` (`8`)
- Box padding: `16`
- Box stroke: `$neutral-200` (`#E0E0E0`), inside, `1`
- Value: Inter `16`, fill `$neutral-800`
- Helper: Inter `12`, fill `$neutral-400`

### StatusIndicator/ON

- ID: `vgcxO`
- Layout: vertical
- Gap: `12`
- Align: center
- Label: `보호 중`, Inter `20`, weight `700`, fill `$success-500`

### StatusIndicator/OFF

- ID: `vKTro`
- Layout: vertical
- Gap: `12`
- Align: center
- Label: `보호 꺼짐`, Inter `20`, weight `700`, fill `$neutral-400`

### PairingCodeDisplay

- ID: `YYhYg`
- Fill: `$neutral-100` (`#F0F0F0`)
- Radius: `$radius-lg` (`16`)
- Padding: `[32, 24]`
- Gap: `8`
- Layout: vertical, center aligned
- Code text: Roboto `40`, weight `700`, fill `$neutral-900`
- Timer text: Inter `17`, fill `$neutral-600`

### LoadingFullScreen

- ID: `k89GYg`
- Width: `200`
- Height: `300`
- Fill: `$neutral-050` (`#F8F8F8`)
- Radius: `$radius-lg` (`16`)
- Gap: `12`
- Layout: vertical, center / center
- Text: Inter `14`, fill `$neutral-600`

### ButtonLoading

- ID: `p0kaZj`
- Height: `52`
- Fill: `$primary-500`
- Radius: `$radius-md`
- Padding: `[0, 24]`
- Gap: `8`
- Text: Inter `14`, weight `700`, fill `$neutral-000`

### SkeletonItem

- ID: `OqRKp`
- Width: `280`
- Fill: `$neutral-000`
- Radius: `$radius-md`
- Padding: `16`
- Gap: `12`
- Layout: vertical
- Shadow: `#00000020`, offset `(0, 2)`, blur `8`

### SeniorCard

- ID: `vlWi0`
- Width: `360`
- Fill: `$neutral-000`
- Radius: `$radius-md`
- Padding: `16`
- Gap: `16`
- Align: center
- Shadow: `#00000020`, offset `(0, 1)`, blur `3`
- Avatar: `48 x 48`, fill `$primary-100`, radius `$radius-full`
- Avatar icon: `elderly`, `28 x 28`, fill `$primary-500`
- Name: Inter `18`, weight `700`, fill `$neutral-900`
- Status row gap: `6`
- Status dot: `10 x 10`, fill `$success-500`
- Status text: Inter `14`, fill `$success-500`
- Detail text: Inter `13`, fill `$neutral-600`
- Chevron icon: `chevron_right`, `20 x 20`, fill `$neutral-400`

### FallHistoryItem

- ID: `YBbXP`
- Width: `360`
- Fill: `$neutral-000`
- Radius: `$radius-md`
- Padding: `16`
- Gap: `12`
- Align: center
- Shadow: `#00000020`, offset `(0, 1)`, blur `3`
- Indicator: `14 x 14`, fill `$danger-500`
- Date text: Inter `15`, weight `500`, fill `$neutral-900`
- Label text: Inter `13`, fill `$neutral-600`

### CountdownTimer

- ID: `u6E17Y`
- Width: `360`
- Height: `500`
- Fill: `$danger-500`
- Radius: `$radius-lg`
- Gap: `24`
- Layout: vertical, center / center
- Warning icon: `warning`, `28 x 28`, fill `$neutral-000`
- Warning text: Inter `18`, weight `600`, fill `$neutral-000`
- Count: Roboto `72`, weight `700`, fill `$neutral-000`
- Subtext: Inter `15`, fill `$neutral-000`, centered, width `300`
- Cancel button: `280 x 64`, fill `$neutral-000`, radius `$radius-md`
- Cancel label: Inter `20`, weight `700`, fill `$danger-500`

### BottomNav

- ID: `PoNeM`
- Width: `360`
- Height: `64`
- Fill: `$neutral-000`
- Align: center
- Justify: `space_around`
- Top stroke: `$neutral-200`, `1`
- Active tab icon: `supervisor_account`, `24 x 24`, fill `$primary-500`
- Active tab label: Inter `12`, fill `$primary-500`
- Inactive tab icon: `notifications`, `24 x 24`, fill `$neutral-600`
- Inactive tab label: Inter `12`, fill `$neutral-600`

### TopAppBar

- ID: `p40OV`
- Width: `360`
- Height: `56`
- Fill: `$neutral-000`
- Padding: `[0, 16]`
- Gap: `8`
- Align: center
- Bottom stroke: `$neutral-200`, `1`
- Back icon: `arrow_back`, `24 x 24`, fill `$neutral-900`
- Title: Inter `18`, weight `600`, fill `$neutral-900`
- More icon: `more_vert`, `24 x 24`, fill `$neutral-900`

### Dialog

- ID: `HGzyo`
- Width: `320`
- Fill: `$neutral-000`
- Radius: `$radius-xl`
- Padding: `24`
- Gap: `16`
- Layout: vertical
- Shadow: `#00000030`, offset `(0, 4)`, blur `12`
- Heading: Inter `18`, weight `700`, fill `$neutral-900`
- Body: Inter `14`, fill `$neutral-600`
- Button row gap: `8`, justify end
- Cancel button height: `40`, radius `$radius-md`, padding `[0, 16]`
- Cancel label: Inter `14`, weight `500`, fill `$primary-500`
- Confirm button height: `40`, fill `$primary-500`, radius `$radius-md`, padding `[0, 16]`
- Confirm label: Inter `14`, weight `600`, fill `$neutral-000`

### Toast

- ID: `A4WX6`
- Width: `320`
- Fill: `$neutral-900`
- Radius: `$radius-md`
- Padding: `[12, 16]`
- Gap: `12`
- Message: Inter `14`, fill `$neutral-000`
- Action: Inter `14`, weight `600`, fill `$primary-300`

### EmptyState

- ID: `XlzUR`
- Width: `280`
- Fill: `$neutral-000`
- Radius: `$radius-lg`
- Padding: `32`
- Gap: `16`
- Layout: vertical, center aligned
- Icon: `shield`, `64 x 64`
- Heading: Inter `16`, weight `700`, fill `$neutral-600`, centered
- Description: Inter `13`, fill `$neutral-400`, centered
- Button height: `48`, fill `$primary-500`, radius `$radius-md`, padding `[0, 20]`
- Button label: Inter `14`, weight `600`, fill `$neutral-000`

### ErrorState

- ID: `YfvlW`
- Width: `280`
- Fill: `$neutral-000`
- Radius: `$radius-lg`
- Padding: `32`
- Gap: `16`
- Layout: vertical, center aligned
- Icon: `warning`, `64 x 64`, fill `$danger-500`
- Heading: Inter `16`, weight `700`, fill `$neutral-900`, centered
- Description: Inter `13`, fill `$neutral-400`, centered
- Button height: `48`, radius `$radius-md`, padding `[0, 20]`
- Button stroke: `$primary-500`, `1.5`
- Button label: Inter `14`, weight `600`, fill `$primary-500`

## App Screens

### Senior — Home

- ID: `Hu1Xd`
- Frame: `390 x 844`
- Fill: `$neutral-000`
- Radius: `32`
- Layout: vertical
- Top area:
  - Status bar height: `44`
  - App name: `SeniorSafe`, Inter `16`, weight `600`, fill `$primary-500`
- Hero area:
  - Uses `StatusIndicator/ON`
  - Heading: `보호 중`, Inter `28`, weight `700`, fill `$success-500`
  - Subtext: `낙상 감지 서비스가 실행 중입니다`, Inter `17`, fill `$neutral-600`
- Bottom area:
  - Padding: `[0, 24, 48, 24]`
  - Gap: `20`
  - Uses `Button/Secondary/Senior`, width `342`, label override `서비스 끄기`
  - Link text: Inter `17`, fill `$primary-500`

### Senior — Pairing Code

- ID: `w8yPxj`
- Frame: `390 x 844`
- Fill: `$neutral-000`
- Radius: `32`
- Layout: vertical
- Uses `TopAppBar`, title override `연결 코드`
- Body padding: `24`
- Heading: Inter `20`, weight `700`, fill `$neutral-900`
- Description: Inter `15`, fill `$neutral-600`
- Uses `PairingCodeDisplay`
- Timer text: Inter `15`, fill `$neutral-400`
- Uses `Button/Secondary/Senior`, width `280`, label override `코드 새로 발급`

### Guardian — Home

- ID: `A5kB13`
- Frame: `390 x 844`
- Fill: `$neutral-050`
- Radius: `32`
- Layout: vertical
- Uses `TopAppBar`, title override `내 어르신`, back icon disabled
- Body padding: `[20, 16]`
- Body gap: `12`
- Section label: Inter `13`, weight `600`, fill `$neutral-400`
- Uses `SeniorCard` list items
- Add button:
  - Height: `48`
  - Radius: `$radius-md`
  - Stroke: `$primary-500`, `1.5`
  - Gap: `6`
  - Icon: `person_add`, `18 x 18`, fill `$primary-500`
  - Label: Inter `14`, weight `600`, fill `$primary-500`
- Uses `BottomNav`

### Guardian — Fall History

- ID: `K4UHN8`
- Frame: `390 x 844`
- Fill: `$neutral-050`
- Radius: `32`
- Layout: vertical
- Uses `TopAppBar`, title override `낙상 이력`
- Senior banner:
  - Fill: `$neutral-000`
  - Padding: `[12, 16]`
  - Gap: `12`
  - Bottom stroke: `$neutral-200`, `1`
  - Avatar: `36 x 36`, fill `$primary-100`, radius `$radius-full`
  - Avatar icon: `elderly`, `20 x 20`, fill `$primary-500`
  - Name: Inter `15`, weight `700`, fill `$neutral-900`
  - Status: Inter `12`, fill `$success-500`
- List area:
  - Padding: `16`
  - Gap: `8`
  - Month label: Inter `13`, weight `600`, fill `$neutral-400`
  - Uses `FallHistoryItem`
- Uses `BottomNav`, active tab override for alert tab

## Notes

- The pen file still contains fall-detection-oriented copy and components, such as `FallHistoryItem`, `CountdownTimer`, and `마지막 낙상`.
- The current MVP focus is unlock activity and inactivity notifications, so these names/copy need product alignment before becoming a strict implementation contract.
- There are no `warning-*` variables in `design.pen`, even though `design/design-system.md` documents warning colors.
