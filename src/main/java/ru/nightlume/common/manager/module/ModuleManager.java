package ru.nightlume.common.manager.module;

import ru.nightlume.api.event.Subscribe;
import ru.nightlume.api.event.impl.KeyEvent;
import ru.nightlume.module.Category;
import ru.nightlume.module.Module;
import ru.nightlume.module.impl.combat.HitboxModule;
import ru.nightlume.module.impl.combat.PredictionsModule;
import ru.nightlume.module.impl.combat.ServerHelperModule;
import ru.nightlume.module.impl.misc.AspectRatioModule;
import ru.nightlume.module.impl.misc.ItemPhysicsModule;
import ru.nightlume.module.impl.player.AutoAcceptModule;
import ru.nightlume.module.impl.player.AutoAuthModule;
import ru.nightlume.module.impl.player.ClickActionModule;
import ru.nightlume.module.impl.render.AmbienceModule;
import ru.nightlume.module.impl.render.ChinaHatModule;
import ru.nightlume.module.impl.render.JumpCirclesModule;
import ru.nightlume.module.impl.render.NimbusModule;
import ru.nightlume.module.impl.render.ParticlesModule;
import ru.nightlume.module.impl.render.TrailsModule;
import ru.nightlume.module.impl.system.ClickGuiModule;
import ru.nightlume.module.impl.system.HudEditorModule;
import ru.nightlume.module.impl.themes.ThemeModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        register(
                new HitboxModule(),
                new PredictionsModule(),
                new ServerHelperModule(),
                new JumpCirclesModule(),
                new TrailsModule(),
                new ChinaHatModule(),
                new NimbusModule(),
                new AmbienceModule(),
                new ParticlesModule(),
                new AutoAcceptModule(),
                new AutoAuthModule(),
                new ClickActionModule(),
                new ItemPhysicsModule(),
                new AspectRatioModule(),
                new HudEditorModule(),
                new ClickGuiModule(),
                new ThemeModule()
        );
    }

    public void register(Module... modules) {
        Collections.addAll(this.modules, modules);

        for (Module module : modules) {
            module.getCategory().addModule(module);
        }
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Category category) {
        List<Module> result = new ArrayList<>();

        for (Module module : modules) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }

        return result;
    }

    public Module getModule(Class<? extends Module> moduleClass) {
        for (Module module : modules) {
            if (module.getClass() == moduleClass) {
                return module;
            }
        }

        return null;
    }

    public Module getModule(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }

        return null;
    }

    public Module getModuleByName(String name) {
        for (Module module : getModules()) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    @Subscribe
    public void onKey(KeyEvent event) {
        int key = event.getKey();
        if (key <= 0) {
            return;
        }

        for (Module module : modules) {
            if (module.getKey() == key) {
                module.toggle();
            }
        }
    }
}