package net.pillowmc.pillow.asm;

import java.util.HashMap;

import org.objectweb.asm.commons.Remapper;

import net.fabricmc.mapping.tree.TinyTree;

public class NameOnlyRemapper extends Remapper {
    private HashMap<String, String> classes, methods, fields;
    public NameOnlyRemapper(TinyTree tree, String from, String to){
        classes = new HashMap<>();
        methods = new HashMap<>();
        fields = new HashMap<>();
        tree.getClasses().forEach(i->{
            classes.put(i.getName(from), i.getName(to));
            i.getMethods().forEach(j->{
                methods.put(j.getName(from)+j.getDescriptor(from), j.getName(to));
            });
            i.getFields().forEach(j->{
                fields.put(j.getName(from), j.getName(to));
            });
        });
    }
    @Override
    public String map(String internalName) {
        return classes.getOrDefault(internalName, internalName);
    }
    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        return mapInvokeDynamicMethodName(name, descriptor);
    }
    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        return fields.getOrDefault(name, name);
    }
    @Override
    public String mapInvokeDynamicMethodName(String name, String descriptor) {
        return methods.getOrDefault(name+descriptor, name);
    }
}
