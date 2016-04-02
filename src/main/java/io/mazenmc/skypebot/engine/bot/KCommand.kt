package io.mazenmc.skypebot.engine.bot

data class CommandInternal(val name: String, val admin: Boolean, val alias: Array<String>,
                                  val command: Boolean, val exact: Boolean, val minArgs: Int = 0) {
    fun name(): String {
        return name
    }

    fun admin(): Boolean {
        return admin
    }

    fun alias(): Array<String> {
        return alias
    }

    fun command(): Boolean {
        return command
    }

    fun exact(): Boolean {
        return exact
    }
}

class CommandBuilder(var name: String) {
    var admin: Boolean = false
    var alias: Array<String> = emptyArray()
    var command: Boolean = true
    var exact: Boolean = false
    var minArgs: Int = 0

    fun admin(admin: Boolean): CommandBuilder {
        this.admin = admin
        return this
    }

    fun alias(alias: Array<String>): CommandBuilder {
        this.alias = alias
        return this
    }

    fun command(command: Boolean): CommandBuilder {
        this.command = command
        return this
    }

    fun exact(exact: Boolean): CommandBuilder {
        this.exact = exact
        return this
    }

    fun min(minArgs: Int): CommandBuilder {
        this.minArgs = minArgs
        return this
    }

    fun internal(): CommandInternal {
        if (name.endsWith("\\b")) {
            name += "\\b"
        }

        return CommandInternal(name, admin, alias, command, exact, minArgs)
    }
}
