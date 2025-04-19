package krazyminer001.playtime.screen.component;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class UpdatableLabelComponent extends BaseComponent {
    protected final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    protected Text cachedText;
    protected Supplier<Text> text;

    protected boolean shadow;
    protected int maxWidth;
    protected final AnimatableProperty<Color> color = AnimatableProperty.of(Color.WHITE);
    protected HorizontalAlignment horizontalTextAlignment = HorizontalAlignment.LEFT;
    protected VerticalAlignment verticalTextAlignment = VerticalAlignment.TOP;

    public UpdatableLabelComponent(Supplier<Text> text) {
        this.text = text;

        this.shadow = false;
        this.maxWidth = Integer.MAX_VALUE;
    }

    public UpdatableLabelComponent text(Supplier<Text> text) {
        this.text = text;
        this.notifyParentIfMounted();
        return this;
    }

    public UpdatableLabelComponent maxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        this.notifyParentIfMounted();
        return this;
    }

    public int maxWidth() {
        return this.maxWidth;
    }

    public UpdatableLabelComponent shadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public boolean shadow() {
        return this.shadow;
    }

    public UpdatableLabelComponent color(Color color) {
        this.color.set(color);
        return this;
    }

    public AnimatableProperty<Color> color() {
        return this.color;
    }

    public UpdatableLabelComponent verticalTextAlignment(VerticalAlignment verticalAlignment) {
        this.verticalTextAlignment = verticalAlignment;
        return this;
    }

    public VerticalAlignment verticalTextAlignment() {
        return this.verticalTextAlignment;
    }

    public UpdatableLabelComponent horizontalTextAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalTextAlignment = horizontalAlignment;
        return this;
    }

    public HorizontalAlignment horizontalTextAlignment() {
        return this.horizontalTextAlignment;
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return Math.min(this.maxWidth, this.textRenderer.getWidth(this.text.get()));
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return this.textRenderer.fontHeight;
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);
        this.color.update(delta);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0, 1 / MinecraftClient.getInstance().getWindow().getScaleFactor(), 0);

        int x = this.x;
        int y = this.y;

        if (this.horizontalSizing.get().isContent()) {
            x += this.horizontalSizing.get().value;
        }
        if (this.verticalSizing.get().isContent()) {
            y += this.verticalSizing.get().value;
        }

        switch (this.verticalTextAlignment) {
            case CENTER -> y += (this.height - (this.textRenderer.fontHeight)) / 2;
            case BOTTOM -> y += this.height - (this.textRenderer.fontHeight);
        }

        final int lambdaX = x;
        final int lambdaY = y;

        int renderX = lambdaX;

        Text text = this.text.get();
        if (text != this.cachedText) {
            this.cachedText = text;
            notifyParentIfMounted();
        }

        switch (this.horizontalTextAlignment) {
            case CENTER -> renderX += (this.width - this.textRenderer.getWidth(text)) / 2;
            case RIGHT -> renderX += this.width - this.textRenderer.getWidth(text);
        }

        context.draw();
        context.drawText(this.textRenderer, text, renderX, lambdaY, this.color.get().argb(), this.shadow);
        context.draw();

        matrices.pop();
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "max-width", UIParsing::parseUnsignedInt, this::maxWidth);
        UIParsing.apply(children, "color", Color::parse, this::color);
        UIParsing.apply(children, "shadow", UIParsing::parseBool, this::shadow);

        UIParsing.apply(children, "vertical-text-alignment", VerticalAlignment::parse, this::verticalTextAlignment);
        UIParsing.apply(children, "horizontal-text-alignment", HorizontalAlignment::parse, this::horizontalTextAlignment);
    }
}
