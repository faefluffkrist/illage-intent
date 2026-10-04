package com.faefluffkrist.illagerhostilities.configscreen;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
public final class ModMenuIntegration implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() { return HostilitiesScreen::new; }
}
