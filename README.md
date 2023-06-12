<!--suppress ALL -->
<p align="center">
    <img src="images/banner.png" alt="PAPIProxyBridge" />
    <a href="https://github.com/WiIIiam278/PAPIProxyBridge/actions/workflows/ci.yml">
        <img src="https://img.shields.io/github/actions/workflow/status/WiIIiam278/PAPIProxyBridge/ci.yml?branch=master&logo=github"/>
    </a>
    <a href="https://repo.william278.net/#releases/net/william278/papiproxybridge">
        <img src="https://repo.william278.net/api/badge/latest/releases/net/william278/husktowns?color=00fb9a&name=Maven&prefix=v" />
    </a> 
    <a href="https://discord.gg/tVYhJfyDWG">
        <img src="https://img.shields.io/discord/818135932103557162.svg?label=&logo=discord&logoColor=fff&color=7389D8&labelColor=6A7EC2" />
    </a>
</p>
<br/>

**PAPIProxyBridge** is a library bridge plugin you install on both your backend and proxy servers that allows proxy plugins to format text with PlaceholderAPI placeholders.

## For server owners
This is a library plugin intended for use with plugins that implement its API. There is nothing to configure.

Install the latest version of the plugin alongside the [PlaceholderAPI plugin](https://www.spigotmc.org/resources/placeholderapi.6245/) on your Spigot (1.16.5+) or the [PlaceholderAPI mod](https://placeholders.pb4.eu/) on your Fabric (1.20) server, then install the plugin on your BungeeCord or Velocity proxy server.

Note this plugin is not a replacement for PlaceholderAPI. You still need to install PlaceholderAPI on your Spigot/Fabric server.

## For developers
PAPIProxyBridge exposes a cross-platform API to let you format text with PlaceholderAPI placeholders.

<details>
<summary>Adding the library to your project</summary>

PAPIProxyBridge is available on `repo.william278.net` ([view javadocs here](https://repo.william278.net/javadoc/releases/net/william278/papiproxybridge/latest)). First, add the maven repository to your `build.gradle`:
```groovy
repositories {
    maven { url 'https://repo.william278.net/releases/' }
}
```

Then add the dependency:
```groovy
dependencies {
    implementation 'net.william278:papiproxybridge:1.2.1'
}
```

</details>

<details>
<summary>Example usage</summary>

The `PlaceholderAPI` class exposes the API for formatting placeholders. At the moment, only singleton non-bracketed placeholders are supported (more in the future).

Get an instance of the class with PlaceholderAPI.getInstance(), then use the `#formatPlaceholders` method to format a string with placeholders on a player (specified with UUID for cross-platform simplicity). 

The method returns a [CompletableFuture](https://www.baeldung.com/java-completablefuture) (since we don't want to lock threads while the proxy networks with players on the backend) that you can use to accept the formatted string.

```java
// Format a string with placeholders
final PlaceholderAPI api = PlaceholderAPI.getInstance();
final UUID player = player.getUniqueId();
api.formatPlaceholders("Hello %player_name%!", player).thenAccept(formatted -> {
    player.sendMessage(formatted);
});
```

Never invoke `#join()` on calls to `#formatPlaceholders`; this is unsafe.

PAPIProxyBridge caches resolved requests for 30000 milliseconds (30 seconds), to avoid causing excessive traffic over your server's network channels. You can adjust how long to cache requests for using the `PlaceholderAPI#setCacheExpiry(long)` method.

</details>
