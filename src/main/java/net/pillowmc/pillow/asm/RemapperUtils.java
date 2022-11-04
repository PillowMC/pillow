package net.pillowmc.pillow.asm;

import net.fabricmc.mapping.tree.TinyTree;
import net.pillowmc.remapper.NameOnlyRemapper;

import java.util.HashMap;

public class RemapperUtils {
    private RemapperUtils(){}
    public static NameOnlyRemapper create(TinyTree tree, String from, String to){
        var classes = new HashMap<String, String>();
        var methods = new HashMap<String, String>();
        var fields = new HashMap<String, String>();
        tree.getClasses().forEach(i->{
            classes.put(i.getName(from), i.getName(to));
            i.getMethods().forEach(j->{
                methods.put(j.getName(from)+j.getDescriptor(from), j.getName(to));
                methods.put(j.getName(from), j.getName(to));
            });
            i.getFields().forEach(j-> fields.put(j.getName(from), j.getName(to)));
        });
        return new NameOnlyRemapper(classes, methods, fields);
    }
}
