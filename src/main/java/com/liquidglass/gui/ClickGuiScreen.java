package com.liquidglass.gui;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.ModuleManager;
import com.liquidglass.module.setting.BooleanSetting;
import com.liquidglass.module.setting.ModeSetting;
import com.liquidglass.module.setting.NumberSetting;
import com.liquidglass.module.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Главное мод-меню в стиле liquid glass.
 * Открывается/закрывается правым Shift (см. KeyboardMixin).
 */
public class ClickGuiScreen extends Screen {

    // Размеры окна (растягиваются, сохраняются между открытиями в рамках сессии)
    private static float W = 372f;
    private static float H = 240f;
    private static final float W_MIN = 300f, W_MAX = 640f;
    private static final float H_MIN = 200f, H_MAX = 470f;

    private static final float TOPBAR = 44f;
    private static final float SIDEBAR = 104f;
    private static final float CAT_H = 26f;
    private static final float CAT_GAP = 3f;
    private static final float ROW_H = 28f;
    private static final float ROW_GAP = 5f;
    private static final float PAD = 10f;
    private static final float GRIP = 14f;
    private static final float SET_H = 15f;
    private static final float TRACK_W = 56f;



    private float winX, winY;
    private static int selectedCategory = 0; // запоминается между открытиями

    // Настройки/бинды
    private Module bindingModule = null;
    private NumberSetting draggingNumber = null;
    private float numTrackX, numTrackW;

    // Прямоугольники кнопок масштаба убраны

    private long lastNano;
    private float openAnim = 0f;
    private float selectorOffset = -1f;
    private float contentScroll = 0f;
    private float contentScrollTarget = 0f;
    private final float[] catHover = new float[Category.values().length];

    private boolean dragging = false;
    private boolean resizing = false;
    private double dragX, dragY;

    public ClickGuiScreen() {
        super(Text.literal("Liquid Glass"));
    }

    @Override
    protected void init() {
        // Размер не больше экрana
        W = Math.min(W, Math.max(W_MIN, this.width - 20));
        H = Math.min(H, Math.max(H_MIN, this.height - 20));
        // Центрируем и держим полностью в экране
        winX = (this.width - W) / 2f;
        winY = (this.height - H) / 2f;
        clampWindow();
        lastNano = System.nanoTime();
    }

    private void clampWindow() {
        winX = Math.max(0, Math.min(winX, this.width - W));
        winY = Math.max(0, Math.min(winY, this.height - H));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private float deltaTime() {
        long now = System.nanoTime();
        float dt = (now - lastNano) / 1_000_000_000f;
        lastNano = now;
        return Math.min(dt, 0.05f);
    }

    private static float approach(float current, float target, float speed, float dt) {
        return current + (target - current) * Math.min(1f, speed * dt);
    }

    private int a(int color) {
        return Theme.withAlpha(color, openAnim);
    }

    private void text(DrawContext ctx, String s, float x, float y, int color) {
        ctx.drawText(this.textRenderer, Fonts.t(s), (int) x, (int) y, color, false);
    }

    private int width(String s) {
        return this.textRenderer.getWidth(Fonts.t(s));
    }

    // Перевод экранных координат мыши в локальные
    private double localX(double mx) {
        return mx;
    }

    private double localY(double my) {
        return my;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float dt = deltaTime();
        float time = System.nanoTime() / 1_000_000_000f;
        openAnim = approach(openAnim, 1f, 12f, dt);

        ctx.fill(0, 0, this.width, this.height, a(Theme.SCREEN_DIM));

        int lmx = (int) Math.round(localX(mouseX));
        int lmy = (int) Math.round(localY(mouseY));

        float scale = 0.96f + 0.04f * openAnim;
        float cx = winX + W / 2f;
        float cy = winY + H / 2f;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(cx, cy, 0);
        ctx.getMatrices().scale(scale, scale, 1f);
        ctx.getMatrices().translate(-cx, -cy, 0);

        RenderUtil.dropShadow(ctx, winX, winY, W, H, 18f, a(Theme.SHADOW));
        RenderUtil.roundedGradient(ctx, winX, winY, W, H, 18f, a(Theme.GLASS_TOP), a(Theme.GLASS_BOTTOM));
        RenderUtil.roundedRect(ctx, winX + 1, winY + 1, W - 2, 24f, 17f, a(Theme.GLASS_HIGHLIGHT));

        renderHeader(ctx, time, lmx, lmy);
        renderSidebar(ctx, lmx, lmy, dt);
        renderContent(ctx, lmx, lmy, dt);
        renderResizeGrip(ctx, lmx, lmy);

        ctx.getMatrices().pop();
    }

    private void renderResizeGrip(DrawContext ctx, int lmx, int lmy) {
        float gx = winX + W - GRIP;
        float gy = winY + H - GRIP;
        boolean hover = inside(lmx, lmy, gx, gy, GRIP, GRIP) || resizing;
        int col = a(Theme.withAlpha(0xFFFFFFFF, hover ? 0.5f : 0.25f));
        for (int i = 0; i < 3; i++) {
            float off = 3f + i * 3.2f;
            RenderUtil.roundedRect(ctx, winX + W - off - 1.6f, winY + H - 4f, 1.6f, 1.6f, 0.8f, col);
            RenderUtil.roundedRect(ctx, winX + W - 4f, winY + H - off - 1.6f, 1.6f, 1.6f, 0.8f, col);
        }
    }

    private void renderHeader(DrawContext ctx, float time, int lmx, int lmy) {
        float tx = winX + PAD;
        float ty = winY + 9;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(tx, ty, 0);
        ctx.getMatrices().scale(1.3f, 1.3f, 1f);
        ctx.drawText(this.textRenderer, Fonts.t("Liquid"), 0, 0, a(Theme.TEXT), false);
        int lw = width("Liquid ");
        float flow = (float) (Math.sin(time * 1.4f) * 0.5f + 0.5f);
        int accent = Theme.mix(Theme.ACCENT_A, Theme.ACCENT_B, flow);
        ctx.drawText(this.textRenderer, Fonts.t("Glass"), lw, 0, a(accent), false);
        ctx.getMatrices().pop();

        text(ctx, "right shift — открыть/закрыть", tx, winY + 26, a(Theme.TEXT_FAINT));

        int enabled = 0;
        for (Module m : ModuleManager.getModules()) if (m.isEnabled()) enabled++;
        String label = enabled + " active";
        int pw = width(label) + 16;
        float px = winX + W - pw - PAD;
        float py = winY + 12;
        RenderUtil.roundedGradient(ctx, px, py, pw, 18f, 9f,
                a(Theme.withAlpha(Theme.ACCENT_A, 0.85f)), a(Theme.withAlpha(Theme.ACCENT_B, 0.85f)));
        text(ctx, label, px + 8, py + 5, a(0xFFFFFFFF));

        // --- Регулятор масштаба удален ---

        RenderUtil.roundedGradient(ctx, winX + PAD, winY + TOPBAR - 2, W - PAD * 2, 1.6f, 0.8f,
                a(Theme.withAlpha(Theme.ACCENT_A, 0.85f)), a(Theme.withAlpha(Theme.ACCENT_B, 0.85f)));
    }



    private void renderSidebar(DrawContext ctx, int mouseX, int mouseY, float dt) {
        Category[] cats = Category.values();
        float sx = winX + PAD;
        float sy = winY + TOPBAR + 6;
        float sw = SIDEBAR - PAD - 6;
        float step = CAT_H + CAT_GAP;

        float targetOffset = selectedCategory * step;
        if (selectorOffset < 0) selectorOffset = targetOffset;
        selectorOffset = approach(selectorOffset, targetOffset, 16f, dt);
        float actualSelY = sy + selectorOffset;

        RenderUtil.roundedGradient(ctx, sx, actualSelY, sw, CAT_H, 9f,
                a(Theme.withAlpha(Theme.ACCENT_A, 0.9f)), a(Theme.withAlpha(Theme.ACCENT_B, 0.9f)));
        RenderUtil.roundedRect(ctx, sx, actualSelY + 6, 3f, CAT_H - 12, 2f, a(0xFFFFFFFF));

        for (int i = 0; i < cats.length; i++) {
            float cyTop = sy + i * step;
            boolean selected = i == selectedCategory;
            boolean hovered = inside(mouseX, mouseY, sx, cyTop, sw, CAT_H);
            catHover[i] = approach(catHover[i], hovered && !selected ? 1f : 0f, 14f, dt);

            if (!selected && catHover[i] > 0.01f) {
                RenderUtil.roundedRect(ctx, sx, cyTop, sw, CAT_H, 9f,
                        a(Theme.withAlpha(0xFFFFFFFF, 0.08f * catHover[i])));
            }

            int textColor = selected ? 0xFFFFFFFF : Theme.mix(Theme.TEXT_DIM, Theme.TEXT, catHover[i]);
            text(ctx, cats[i].displayName, sx + 12, cyTop + (CAT_H - this.textRenderer.fontHeight) / 2f, a(textColor));
        }

        RenderUtil.roundedRect(ctx, winX + SIDEBAR, winY + TOPBAR + 4, 1f, H - TOPBAR - 14, 0, a(Theme.BORDER_SOFT));
    }

    private float panelFull(Module m) {
        return (1 + m.settings.size()) * SET_H + 6f;
    }

    private void renderContent(DrawContext ctx, int mouseX, int mouseY, float dt) {
        List<Module> modules = ModuleManager.getByCategory(Category.values()[selectedCategory]);

        float listX = winX + SIDEBAR + 8;
        float listY = winY + TOPBAR + 8;
        float listW = W - SIDEBAR - 8 - PAD;
        float listH = H - TOPBAR - 8 - PAD;

        float totalH = 0f;
        for (Module m : modules) {
            totalH += ROW_H + ROW_GAP + (m.expanded ? panelFull(m) : 0f);
        }
        float maxScroll = Math.max(0f, totalH - listH);
        contentScrollTarget = Math.max(0f, Math.min(contentScrollTarget, maxScroll));
        contentScroll = approach(contentScroll, contentScrollTarget, 16f, dt);

        ctx.enableScissor((int) listX, (int) listY, (int) (listX + listW), (int) (listY + listH));

        float y = listY - contentScroll;
        for (Module m : modules) {
            m.toggleAnim = approach(m.toggleAnim, m.isEnabled() ? 1f : 0f, 16f, dt);
            m.expandAnim = approach(m.expandAnim, m.expanded ? 1f : 0f, 14f, dt);
            float panelH = m.expandAnim * panelFull(m);

            boolean visible = !(y + ROW_H < listY || y > listY + listH);
            if (visible) {
                boolean hovered = inside(mouseX, mouseY, listX, y, listW, ROW_H)
                        && mouseY >= listY && mouseY <= listY + listH;
                m.hoverAnim = approach(m.hoverAnim, hovered ? 1f : 0f, 14f, dt);

                int rowBg = Theme.mix(Theme.withAlpha(0xFFFFFFFF, 0.05f),
                        Theme.withAlpha(0xFFFFFFFF, 0.12f), m.hoverAnim);
                if (m.isEnabled()) {
                    rowBg = Theme.mix(rowBg, Theme.withAlpha(Theme.ACCENT_A, 0.18f), 0.6f);
                }
                RenderUtil.roundedRect(ctx, listX, y, listW, ROW_H, 8f, a(rowBg));

                int nameColor = m.isEnabled() ? Theme.TEXT : Theme.mix(Theme.TEXT_DIM, Theme.TEXT, m.hoverAnim);
                text(ctx, m.getName(), listX + 10, y + 5, a(nameColor));
                text(ctx, m.getDescription(), listX + 10, y + 16, a(Theme.TEXT_FAINT));

                float pillW = 22f, pillH = 12f;
                float pillX = listX + listW - pillW - 10f;
                float pillY = y + (ROW_H - pillH) / 2f;
                int offCol = Theme.withAlpha(0xFFFFFFFF, 0.14f);
                int trackTop = Theme.mix(offCol, Theme.ACCENT_A, m.toggleAnim);
                int trackBot = Theme.mix(offCol, Theme.ACCENT_B, m.toggleAnim);
                RenderUtil.roundedGradient(ctx, pillX, pillY, pillW, pillH, pillH / 2f, a(trackTop), a(trackBot));
                float knob = 2f + m.toggleAnim * (pillW - pillH);
                RenderUtil.roundedRect(ctx, pillX + knob, pillY + 2f, pillH - 4f, pillH - 4f, (pillH - 4f) / 2f, a(0xFFFFFFFF));

                if (!m.settings.isEmpty()) {
                    String chev = m.expanded ? "v" : ">";
                    text(ctx, chev, pillX - 12, y + 5, a(Theme.TEXT_FAINT));
                }
            } else {
                m.hoverAnim = approach(m.hoverAnim, 0f, 14f, dt);
            }

            if (panelH > 0.6f) {
                ctx.enableScissor((int) listX, (int) (y + ROW_H), (int) (listX + listW), (int) (y + ROW_H + panelH));
                renderSettings(ctx, m, listX, listW, y + ROW_H + 3f, mouseX, mouseY);
                ctx.disableScissor();
            }

            y += ROW_H + ROW_GAP + panelH;
        }

        ctx.disableScissor();
    }

    private void renderSettings(DrawContext ctx, Module m, float listX, float listW, float baseY, int mouseX, int mouseY) {
        RenderUtil.roundedRect(ctx, listX + 4, baseY - 2, listW - 8, panelFull(m) - 4, 6f, a(0x40000000));

        // Бинд
        String bindVal = bindingModule == m ? "нажми клавишу..." : keyName(m.keyBind);
        text(ctx, "Бинд", listX + 14, baseY + 4, a(Theme.TEXT_DIM));
        int bw = width(bindVal);
        text(ctx, bindVal, listX + listW - 12 - bw, baseY + 4, a(bindingModule == m ? Theme.ACCENT_A : Theme.TEXT));

        for (int i = 0; i < m.settings.size(); i++) {
            Setting s = m.settings.get(i);
            float ry = baseY + (i + 1) * SET_H;
            text(ctx, s.name, listX + 14, ry + 4, a(Theme.TEXT_DIM));

            if (s instanceof BooleanSetting bs) {
                float bx = listX + listW - 12 - 16;
                RenderUtil.roundedRect(ctx, bx, ry + 1, 16, 11, 5f,
                        a(bs.value ? Theme.withAlpha(Theme.ACCENT_A, 0.9f) : Theme.withAlpha(0xFFFFFFFF, 0.12f)));
                float k = bs.value ? 7f : 1.5f;
                RenderUtil.roundedRect(ctx, bx + k, ry + 2.5f, 8, 8, 4f, a(0xFFFFFFFF));
            } else if (s instanceof ModeSetting ms) {
                int vw = width(ms.get());
                text(ctx, ms.get(), listX + listW - 12 - vw, ry + 4, a(Theme.ACCENT_A));
            } else if (s instanceof NumberSetting ns) {
                String val = ns.display();
                int vw = width(val);
                float trackX = listX + listW - 12 - vw - 6 - TRACK_W;
                RenderUtil.roundedRect(ctx, trackX, ry + 5, TRACK_W, 3, 1.5f, a(Theme.withAlpha(0xFFFFFFFF, 0.15f)));
                float fill = (float) (TRACK_W * ns.getFraction());
                RenderUtil.roundedGradient(ctx, trackX, ry + 5, Math.max(2, fill), 3, 1.5f,
                        a(Theme.ACCENT_A), a(Theme.ACCENT_B));
                RenderUtil.roundedRect(ctx, trackX + fill - 2.5f, ry + 2.5f, 5, 8, 2.5f, a(0xFFFFFFFF));
                text(ctx, val, listX + listW - 12 - vw, ry + 4, a(Theme.TEXT));
            }
        }
    }

    private static String keyName(int key) {
        if (key < 0) return "—";
        String n = GLFW.glfwGetKeyName(key, 0);
        if (n != null && !n.isBlank()) return n.toUpperCase();
        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_F -> "F";
            default -> "K" + key;
        };
    }

    @Override
    public boolean mouseClicked(double mouseXRaw, double mouseYRaw, int button) {
        double mouseX = localX(mouseXRaw);
        double mouseY = localY(mouseYRaw);

        if (button == 0) {
            if (inside(mouseX, mouseY, winX + W - GRIP, winY + H - GRIP, GRIP, GRIP)) {
                resizing = true;
                return true;
            }


            // Категории
            Category[] cats = Category.values();
            float sx = winX + PAD;
            float sy = winY + TOPBAR + 6;
            float sw = SIDEBAR - PAD - 6;
            float step = CAT_H + CAT_GAP;
            for (int i = 0; i < cats.length; i++) {
                float cyTop = sy + i * step;
                if (inside(mouseX, mouseY, sx, cyTop, sw, CAT_H)) {
                    if (selectedCategory != i) {
                        selectedCategory = i;
                        contentScroll = 0f;
                        contentScrollTarget = 0f;
                    }
                    return true;
                }
            }
        }

        // Модули и настройки (левый — тоггл/изменение, правый — раскрыть)
        if (handleContentClick(mouseX, mouseY, button)) return true;

        // Перетаскивание окна за шапку
        if (button == 0 && inside(mouseX, mouseY, winX, winY, W, TOPBAR)) {
            dragging = true;
            dragX = mouseX - winX;
            dragY = mouseY - winY;
            return true;
        }

        return super.mouseClicked(mouseXRaw, mouseYRaw, button);
    }

    private boolean handleContentClick(double mouseX, double mouseY, int button) {
        List<Module> modules = ModuleManager.getByCategory(Category.values()[selectedCategory]);
        float listX = winX + SIDEBAR + 8;
        float listY = winY + TOPBAR + 8;
        float listW = W - SIDEBAR - 8 - PAD;
        float listH = H - TOPBAR - 8 - PAD;
        if (mouseX < listX || mouseX > listX + listW || mouseY < listY || mouseY > listY + listH) return false;

        float y = listY - contentScroll;
        for (Module m : modules) {
            float panelH = m.expandAnim * panelFull(m);

            if (inside(mouseX, mouseY, listX, y, listW, ROW_H)) {
                if (button == 1) {
                    m.expanded = !m.expanded;
                } else {
                    m.toggle();
                }
                return true;
            }

            if (panelH > 3f && mouseY >= y + ROW_H && mouseY <= y + ROW_H + panelH) {
                float baseY = y + ROW_H + 3f;
                int idx = (int) Math.floor((mouseY - baseY) / SET_H);
                if (idx == 0) {
                    if (button == 1) m.keyBind = -1;
                    else bindingModule = m;
                    return true;
                }
                int si = idx - 1;
                if (si >= 0 && si < m.settings.size()) {
                    Setting s = m.settings.get(si);
                    if (s instanceof BooleanSetting bs) {
                        bs.toggle();
                    } else if (s instanceof ModeSetting ms) {
                        ms.cycle(button == 1 ? -1 : 1);
                    } else if (s instanceof NumberSetting ns) {
                        int vw = width(ns.display());
                        float trackX = listX + listW - 12 - vw - 6 - TRACK_W;
                        draggingNumber = ns;
                        numTrackX = trackX;
                        numTrackW = TRACK_W;
                        ns.setFromFraction((mouseX - trackX) / TRACK_W);
                    }
                }
                return true;
            }

            y += ROW_H + ROW_GAP + panelH;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
            resizing = false;
            draggingNumber = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingNumber != null) {
            draggingNumber.setFromFraction((localX(mouseX) - numTrackX) / numTrackW);
            return true;
        }
        if (resizing) {
            W = Math.max(W_MIN, Math.min(W_MAX, (float) localX(mouseX) - winX));
            H = Math.max(H_MIN, Math.min(H_MAX, (float) localY(mouseY) - winY));
            W = Math.min(W, this.width - winX);
            H = Math.min(H, this.height - winY);
            return true;
        }
        if (dragging) {
            winX = (float) (localX(mouseX) - dragX);
            winY = (float) (localY(mouseY) - dragY);
            clampWindow();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        contentScrollTarget -= (float) amount * 22f;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Назначение бинда
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE
                    || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                bindingModule.keyBind = -1;
            } else {
                bindingModule.keyBind = keyCode;
            }
            bindingModule = null;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP) {
            selectedCategory = (selectedCategory - 1 + Category.values().length) % Category.values().length;
            contentScrollTarget = 0f;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            selectedCategory = (selectedCategory + 1) % Category.values().length;
            contentScrollTarget = 0f;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static boolean inside(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
