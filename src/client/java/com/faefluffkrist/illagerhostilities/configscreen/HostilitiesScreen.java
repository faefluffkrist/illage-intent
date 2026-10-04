package com.faefluffkrist.illagerhostilities.configscreen;

import com.faefluffkrist.illagerhostilities.HostilitiesConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.*;

public final class HostilitiesScreen extends ScrollingConfigScreen {
    private final Screen parent;
    private final HostilitiesScreen main;
    private HostilitiesConfig draft;
    private int category;
    private String status="";
    private List<Row> rows=List.of();
    private record Row(String name,String description,BooleanSupplier get,Consumer<Boolean> set, boolean master) {}
    private static final String[] TABS={"General","Targets","Vanilla Illagers"};
    public HostilitiesScreen(Screen parent){this(parent,null,0,HostilitiesConfig.editable);}
    private HostilitiesScreen(Screen parent,HostilitiesScreen main,int category,HostilitiesConfig source){
        super(Component.literal(category==3?"Friends & Foes Settings":category==4?"It Takes a Pillage Continuation Settings":"Ill(age) Intent Configuration"));
        this.parent=parent;this.main=main;this.category=category;this.draft=HostilitiesConfig.copy(source);
    }
    private boolean remote(){return minecraft.getConnection()!=null&&!minecraft.hasSingleplayerServer();}
    private boolean loaded(String id){return FabricLoader.getInstance().isModLoaded(id);}
    private boolean available(){return category!=3&&category!=4||loaded(category==3?"friendsandfoes":"takesapillage");}
    private boolean support(){return category==3?draft.friendsAndFoes:category!=4||draft.takesAPillage;}
    private List<Row> options(){
        List<Row> r=new ArrayList<>();
        if(category==0) r.add(new Row("Enable Ill(age) Intent","Allow supported illagers and their allies to proactively attack the selected hostile mobs.",()->draft.enabled,v->draft.enabled=v,true));
        if(category==1){
            r.add(new Row("Undead","Target undead mobs. Zombie horses and skeleton horses are always excluded.",()->draft.undead,v->draft.undead=v,false));
            r.add(new Row("Creepers","Allow supported attackers to target creepers.",()->draft.creepers,v->draft.creepers=v,false));
            r.add(new Row("Spiders & Cave Spiders","Allow supported attackers to target both spider types.",()->draft.spiders,v->draft.spiders=v,false));
            r.add(new Row("Silverfish","Allow supported attackers to target silverfish.",()->draft.silverfish,v->draft.silverfish=v,false));
        }
        if(category==3) r.add(new Row("Enable Friends & Foes Support","Enable the attackers below when Friends & Foes is loaded.",()->draft.friendsAndFoes,v->draft.friendsAndFoes=v,true));
        if(category==4) r.add(new Row("Enable It Takes a Pillage Support","Enable the attackers below when It Takes a Pillage Continuation is loaded.",()->draft.takesAPillage,v->draft.takesAPillage=v,true));
        if(category>=2){
            String ns=category==2?"minecraft:":category==3?"friendsandfoes:":"takesapillage:";
            String[] names=category==2?new String[]{"pillager","vindicator","evoker","illusioner","vex","witch","ravager"}:category==3?new String[]{"iceologer","illusioner"}:new String[]{"archer","legioner","skirmisher"};
            for(String name:names){String id=ns+name;
                String description="Allow "+pretty(name).toLowerCase(Locale.ROOT)+" mobs to attack the selected targets.";
                if(name.equals("witch")) description="Use damaging potion choices against undead and poison-immune targets.";
                r.add(new Row(pretty(name),description,()->Boolean.TRUE.equals(draft.attackers.get(id)),v->draft.attackers.put(id,v),false));
            }
        }
        return r;
    }
    private static String pretty(String name){return Character.toUpperCase(name.charAt(0))+name.substring(1);}
    private List<String> wrap(String text,int width){
        List<String> result=new ArrayList<>();String line="";
        for(String word:text.split("\\s+")){String next=line.isEmpty()?word:line+" "+word;if(!line.isEmpty()&&font.width(next)>width){result.add(line);line=word;}else line=next;}
        if(!line.isEmpty())result.add(line);return result;
    }
    private int rowHeight(Row row){int textWidth=listWidth-84;return 14+wrap(row.name,textWidth).size()*10+wrap(row.description,textWidth).size()*10+10;}
    private boolean editable(Row row){return !remote()&&available()&&(main==null||row.master||support());}
    @Override protected void init(){
        int w=Math.min(500,width-48),x=(width-w)/2;
        if(main==null){
            int tab=(w-8)/3;
            for(int i=0;i<TABS.length;i++){final int selected=i;Button b=addRenderableWidget(Button.builder(Component.literal(TABS[i]),button->{category=selected;scrollPixels=0;status="";rebuildWidgets();}).bounds(x+i*(tab+4),34,tab,20).build());b.active=category!=i;}
            compatibilityButton("Friends & Foes","friendsandfoes",3,x,height-88,(w-4)/2);
            compatibilityButton("It Takes a Pillage","takesapillage",4,x+(w+4)/2,height-88,(w-4)/2);
        }
        rows=options();
        layoutContent(0,main==null?82:62,height-(main==null?102:72),500);
        int content=rows.stream().mapToInt(this::rowHeight).sum();
        layoutContent(content,listTop,listBottom,500);
        int y=listTop-scrollPixels;
        for(Row row:rows){int h=rowHeight(row),by=y+Math.max(6,(h-10-20)/2);
            if(controlVisible(by,20)){
                boolean enabled=row.get.getAsBoolean(),canEdit=editable(row);
                Component text=Component.literal(enabled?"ON":"OFF").withStyle(canEdit?(enabled?ChatFormatting.GREEN:ChatFormatting.RED):ChatFormatting.GRAY);
                Button b=addRenderableWidget(Button.builder(text,button->{row.set.accept(!row.get.getAsBoolean());status="";rebuildWidgets();}).bounds(listX+listWidth-60,by,60,20).build());b.active=canEdit;
            }y+=h;
        }
        int bw=Math.min(90,(width-56)/3),left=width/2-(3*bw+12)/2;
        Button defaults=addRenderableWidget(Button.builder(Component.literal("Defaults"),b->defaults()).bounds(left,height-28,bw,20).build());defaults.active=!remote()&&available();
        Button save=addRenderableWidget(Button.builder(Component.literal(main==null?"Save":"Apply"),b->save()).bounds(left+bw+6,height-28,bw,20).build());save.active=!remote()&&available();
        addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(left+2*(bw+6),height-28,bw,20).build());
    }
    private void compatibilityButton(String name,String id,int destination,int x,int y,int w){
        boolean detected=loaded(id);
        Component label=Component.literal(name).withStyle(detected?ChatFormatting.WHITE:ChatFormatting.GRAY);
        Button b=addRenderableWidget(Button.builder(label,button->minecraft.gui.setScreen(new HostilitiesScreen(this,this,destination,draft))).bounds(x,y,w,20).build());
        b.active=detected;
        b.setTooltip(Tooltip.create(Component.literal(detected?"Open "+name+" settings":name+" is not loaded")));
    }
    private void defaults(){
        HostilitiesConfig d=new HostilitiesConfig();
        if(category==0)draft.enabled=d.enabled;
        else if(category==1){draft.undead=d.undead;draft.creepers=d.creepers;draft.spiders=d.spiders;draft.silverfish=d.silverfish;}
        else{String ns=category==2?"minecraft:":category==3?"friendsandfoes:":"takesapillage:";d.attackers.forEach((id,v)->{if(id.startsWith(ns))draft.attackers.put(id,v);});if(category==3)draft.friendsAndFoes=true;if(category==4)draft.takesAPillage=true;}
        status="Defaults restored for this page";rebuildWidgets();
    }
    private void save(){
        if(remote()||!available())return;
        if(main!=null){
            String ns=category==3?"friendsandfoes:":"takesapillage:";
            draft.attackers.forEach((id,v)->{if(id.startsWith(ns))main.draft.attackers.put(id,v);});
            if(category==3)main.draft.friendsAndFoes=draft.friendsAndFoes;else main.draft.takesAPillage=draft.takesAPillage;
            main.status="Compatibility changes applied; Save to keep them";minecraft.gui.setScreen(parent);return;
        }
        if(HostilitiesConfig.same(draft,HostilitiesConfig.editable)){minecraft.gui.setScreen(parent);return;}
        HostilitiesConfig old=HostilitiesConfig.editable;HostilitiesConfig.editable=HostilitiesConfig.copy(draft);
        try{HostilitiesConfig.save();minecraft.gui.setScreen(new RelogNoticeScreen(parent));}
        catch(RuntimeException e){HostilitiesConfig.editable=old;status="Could not save settings; changes are still pending";rebuildWidgets();}
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        super.extractRenderState(g,mx,my,delta);
        g.centeredText(font,title,width/2,12,0xFFFFFFFF);
        g.centeredText(font,Component.literal(main==null?TABS[category]+" settings":available()?"Compatibility mod detected":"Compatibility mod is not loaded"),width/2,main==null?64:40,0xFFFFE0A1);
        g.enableScissor(listX,listTop,listX+listWidth,listBottom);
        int y=listTop-scrollPixels;
        for(Row row:rows){int h=rowHeight(row);
            if(y+h>=listTop&&y<listBottom){
                int textWidth=listWidth-84;boolean enabled=editable(row);
                g.fill(listX,y,listX+listWidth-70,y+h-8,0x40333C42);
                int ty=y+6;
                for(String line:wrap(row.name,textWidth)){g.text(font,Component.literal(line),listX+6,ty,enabled?0xFFFFE0A1:0xFF888888);ty+=10;}
                ty+=3;
                for(String line:wrap(row.description,textWidth)){g.text(font,Component.literal(line),listX+6,ty,enabled?0xFFB7C7CC:0xFF888888);ty+=10;}
            }y+=h;
        }
        g.disableScissor();drawScrollbar(g);
        String footer=remote()?"Server controls gameplay; edit its config":!status.isEmpty()?status:main!=null?"Apply returns changes to the main menu": "Save changes, then reopen your world";
        g.centeredText(font,Component.literal(footer),width/2,height-54,0xFFB7C7CC);
        g.centeredText(font,Component.literal("Back discards unsaved changes"),width/2,height-42,0xFF888888);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
