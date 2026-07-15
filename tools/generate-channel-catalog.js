const fs = require("fs");
const vm = require("vm");

const [sourcePath, outputPath] = process.argv.slice(2);
if (!sourcePath || !outputPath) {
  console.error("Usage: node generate-channel-catalog.js SOURCE_JS OUTPUT_KT");
  process.exit(64);
}

const source = fs.readFileSync(sourcePath, "utf8");
const catalog = vm.runInNewContext(`${source}\nCHANNELS_DATA;`, {});
const quote = (value) => JSON.stringify(String(value ?? "").trim());
const lines = [
  "package com.tntv.tv",
  "",
  "// Generated from the TRIONINE TV website catalog. Keep the website as the source of truth.",
  "val channelCategories = listOf(",
];

for (const category of catalog.categories) {
  lines.push("    ChannelCategory(");
  lines.push(`        name = ${quote(category.name)},`);
  lines.push("        channels = listOf(");

  for (const channel of category.channels) {
    lines.push("            Channel(");
    lines.push(`                id = ${quote(channel.id)},`);
    lines.push(`                name = ${quote(channel.name)},`);
    lines.push(`                shortName = ${quote(channel.shortName)},`);
    lines.push(`                category = ${quote(category.name)},`);
    lines.push(`                logo = ${quote(channel.logo)},`);
    lines.push("                streams = listOf(");

    for (const stream of channel.streams ?? []) {
      lines.push(`                    StreamSource(label = ${quote(stream.label)}, url = ${quote(stream.url)}),`);
    }

    lines.push("                ),");
    lines.push("            ),");
  }

  lines.push("        ),");
  lines.push("    ),");
}

lines.push(");", "");
fs.writeFileSync(outputPath, lines.join("\n"));
