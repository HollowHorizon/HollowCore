gui {
    gui_scale = 2

    rect("main") {
        label("title") {
            text = "Example GUI"
        }
        rect("buttons_menu") {
            button("button1") { text = "Singleplayer" }
            button("button2") { text = "Multiplayer" }
            button("button3") { text = "Options" }
            button("button4") { text = "Quit" }
        }
        custom("custom") {
            val stack = it.matrixStack
            val font = it.fontRenderer

            stack.pushPose()
            stack.translate(0.0, 0.0, 0.0)

            font.draw(stack, "Hello, World!", 0f, 0f, 0xFFFFFF)

            stack.popPose()
        }
    }

    rect("context_menu", hidden=true) {
        label("title") {
            text = "Context menu"
        }
        button("button1") { text = "Button 1" }
        button("button2") { text = "Button 2" }
        button("button3") { text = "Button 3" }
        button("button4") { text = "Button 4" }
    }
}

style {
    rect "main" {
        pos = 0.px x 0.px //can be 0.sc (screen) or 0.pr (parent) or 0.px (pixel) or 0.cs (cursor)
        size = 100.pr x 100.pr //can be 100.sc (screen) or 100.pr (parent) or 100.px (pixel)
        color = 0x000000FF.toRGBA()
    }
    rect "buttons_menu" {
        pos = 0.pr x 0.pr
        size = 90.pr x 90.pr
        color = 0x000000FF.toRGBA()

        content_spacing = Spacing.STRETCH // Horizontal and vertical spacing between elements
    }
    rect "context_menu" {
        pos = 0.pr x 0.pr
        size = 90.pr x 90.pr
        color = 0x000000FF.toRGBA()

        content_spacing = Spacing.VERTICAL // Horizontal and vertical spacing between elements
    }
    label "title" {
        align = Align.CENTER
        font = "minecraft:default"
        scale = 2
        z_layer = 3
    }
    button "*" {
        pos = 0.pr x 0.pr
        size = 100.pr x 10.pr
        text_color = 0x000000FF.toRGBA()
        hover_text_color = 0x000000FF.toRGBA()
        font = "minecraft:default"
        scale = 1
        texture = "hc:textures/gui/button.png".rl

        margin = 1.pr
        padding = 1.pr

        animation {
            duration = 0.2
            easing = Easing.EASE_OUT
            property = Property.TEXTURE_UV // also can be Property.POSITION, Property.SIZE, Property.TEXT_COLOR, Property.HOVER_TEXT_COLOR, Property.TEXTURE, Property.TEXTURE_UV
            from = 0.0 x 0.0
            to = 0.0 x 0.5
        }
    }
    custom "*" {
        pos = 0.pr x 0.pr
        size = 100.pr x 100.pr
        z_layer = 2
    }
}

action {
    button "button1" {
        var lastClickText = ""
        onClick {
            HollowCore.LOGGER.info("Button 1 clicked")
        }
        onHover {
            lastClickText = label("title").text
            label("title").text = "Hovered button 1"
        }
        onUnhover {
            label("title").text = lastClickText
        }
    }
    button "button2" {
        onClick {
            HollowCore.LOGGER.info("Button 2 clicked")
        }
    }
    button "button3" {
        onClick {
            HollowCore.LOGGER.info("Button 3 clicked")
        }
    }
    button "button4" {
        onClick {
            HollowCore.LOGGER.info("Button 4 clicked")
        }
    }

    onClick {
        val menu = rect("context_menu")

        if(button == 1 && !menu.isHovered) {
            menu.hidden = !menu.hidden
        } else {
            menu.hidden = true
        }
    }
}