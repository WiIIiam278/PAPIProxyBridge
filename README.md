<p align="center">
    <img src="images/banner.png" alt="PAPIProxyBridge" />
    <a href="https://github.com/WiIIiam278/PAPIProxyBridge/actions/workflows/java_ci.yml">
        <img src="https://img.shields.io/github/actions/workflow/status/WiIIiam278/Velocitab/java_ci.yml?branch=master&logo=github"/>
    </a>
    <a href="https://jitpack.io/#net.william278/PAPIProxyBridge">
        <img src="https://img.shields.io/jitpack/version/net.william278/PAPIProxyBridge?color=%2300fb9a&label=api&logo=gradle" />
    </a> 
    <a href="https://discord.gg/tVYhJfyDWG">
        <img src="https://img.shields.io/discord/818135932103557162.svg?label=&logo=discord&logoColor=fff&color=7389D8&labelColor=6A7EC2" />
    </a>
</p>
<br/>
PAPIProxyBridge is a library bridge plugin you install on both your backend and proxy servers that allows proxy plugins to format text with Placeholder API placeholders.

## For server owners
This is a library plugin intended for use with plugins that implement its API. There is nothing to configure.

Install the latest version of the plugin alongside PlaceholderAPI on your Spigot server, then install the plugin on your BungeeCord or Velocity proxy server.

Note this plugin is not a replacement for PlaceholderAPI. You still need to install PlaceholderAPI on your Spigot server.

## For developers
PAPIProxyBridge exposes a cross-platform API to let you format text with PlaceholderAPI placeholders.

<details>
<summary>Adding the library to your project</summary>

PAPIProxyBridge is available on [Jitpack](https://jitpack.io/#net.william278/PAPIProxyBridge). First, add the maven repository to your `build.gradle`:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

Then add the dependency:
```groovy
dependencies {
    implementation 'net.william278:PAPIProxyBridge:1.0'
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

PAPIProxyBridge caches resolved requests for 30000 milliseconds (30 seconds), to avoid causing excessive traffic over your servers network channels.

</details>