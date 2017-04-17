package targoss.hardcorealchemy.coremod.transform;

import static org.objectweb.asm.Opcodes.ASM4;

import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public class TUseless extends ClassNode implements IClassTransformer {
	
	public boolean foundAClass;
	
	public TUseless() {
		super(ASM4);
		this.foundAClass = false;
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		//TODO: Congratulations. It works. Now, add Pam's Harvestcraft and Tough as Nails to the workspace
		// TaN is in the workspace, but I'm 99% sure a change to the name of a decompiled class broke the 1.10 dev workspace
		if (!foundAClass) {
			foundAClass = true;
			System.out.println("************FOUND SOMETHING TO TRANSFORM************");
			System.out.println("name: " + name);
			System.out.println("transformedName: " + transformedName);
			System.out.println("************             YAY            ************");
		}
		return basicClass;
	}
	
}
