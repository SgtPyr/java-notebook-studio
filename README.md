# Java Notebook Studio

A Java 21 command-line tool for inspecting and validating notebook stories stored in JSON.

## Features

- List stories
- Search story titles and bodies
- Display an individual story
- Validate notebook structure and required fields
- Export stories to a separate JSON file
- Automated tests using JUnit 5

## Requirements

- Java 21 or newer
- Gradle 9.x
- Access to your notebook JSON file

## Default notebook path

`/storage/emulated/0/vide coding project/for you/stories/stories.json`

## Usage

Validate the notebook:

```sh
gradle run --args="validate"
```

List the first five stories:

```sh
gradle run --args="list 5"
```

Search stories:

```sh
gradle run --args="search friendship"
```

Display story number one:

```sh
gradle run --args="show 1"
```

Export stories:

```sh
gradle run --args="export /path/to/export.json"
```

Run automated tests:

```sh
gradle test
```

## Use another notebook file

```sh
gradle run --args="validate --file /path/to/stories.json"
```

## Safety

The tool does not modify the source notebook. Export writes to a separate destination file.

## License

MIT
