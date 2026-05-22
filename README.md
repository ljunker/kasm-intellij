# KASM Language for IntelliJ

IntelliJ language support for `.kasm` source files.

KASM is the assembly language used by [ljunker/Kasm](https://github.com/ljunker/Kasm). This plugin adds editor
support for writing KASM programs in IntelliJ-based IDEs.

## Features

- File type support for `.kasm`
- Syntax highlighting for instructions, registers, labels, numbers, comments, and memory-address brackets
- Code completion for KASM instructions and registers
- Instruction signature help based on the KASM language reference
- Formatting with labels at column zero and instructions indented by one level
- KASM run configurations and source-level debugger sessions

## Example

```kasm
start:
  MOV R0, 4
  MOV R1, 1

loop:
  PRINT R0
  DEC R0
  JNZ R0, loop
  HALT
```

## Installation

Install the plugin from JetBrains Marketplace once it is published there.

For a local build, build the KASM library from the sibling KASM checkout first:

```shell
../Kasm/gradlew jar
./gradlew buildPlugin
```

Then install the ZIP from `build/distributions/` in IntelliJ:

1. Open `Settings` or `Preferences`.
2. Go to `Plugins`.
3. Open the plugin menu and choose `Install Plugin from Disk...`.
4. Select the generated ZIP.

## Development

The debugger bundles the KASM library from the sibling `../Kasm` checkout by default. Build that jar before running
the plugin build:

```shell
../Kasm/gradlew jar
./gradlew check buildPlugin
```

Start a development IDE with the plugin loaded:

```shell
./gradlew runIde
```

The plugin implementation lives in `src/main/kotlin/de/ljunker/kasm/intellij/`. The IntelliJ extension registrations
are defined in `src/main/resources/META-INF/plugin.xml`.

## Debugging

The plugin uses the headless KASM debug session API for source-level IntelliJ debugger sessions.

1. Open or select a `.kasm` file.
2. Create or reuse the generated KASM run configuration for that file.
3. Set breakpoints on executable KASM instruction lines.
4. Start the configuration with the IntelliJ Debug action.

The debug tool window stops at KASM source locations, supports resume and stepping, shows registers, flags, stack
values, and non-zero data memory, and prints KASM program output in the debug console.

## Publishing

The first JetBrains Marketplace upload must be created manually from the ZIP in `build/distributions/`. After that,
`.github/workflows/plugin.yml` publishes tagged releases.

Before publishing a new version:

1. Set `pluginVersion` in `gradle.properties`.
2. Update `changeNotes` in `build.gradle.kts`.
3. Commit and push the release changes.
4. Tag the release commit with the same version prefixed by `v`, for example `v1.0.1`.
5. Push the tag.

The publish workflow expects these GitHub Actions repository secrets:

| Secret                 | Purpose                                                 |
|------------------------|---------------------------------------------------------|
| `PUBLISH_TOKEN`        | JetBrains Marketplace personal access token             |
| `CERTIFICATE_CHAIN`    | Plugin signing certificate chain                        |
| `PRIVATE_KEY`          | Plugin signing private key                              |
| `PRIVATE_KEY_PASSWORD` | Password for the private key, when the key is encrypted |

Marketplace versions are immutable after upload. Increase `pluginVersion` for every published update.
