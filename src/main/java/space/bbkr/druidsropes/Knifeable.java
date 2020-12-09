package space.bbkr.druidsropes;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

public interface Knifeable {
	ActionResult onKnife(ItemUsageContext context);
}
