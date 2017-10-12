package adv_director.rw2.api.client.gui.panels.lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import adv_director.rw2.api.client.gui.events.PanelEvent;
import adv_director.rw2.api.client.gui.misc.ComparatorGuiDepth;
import adv_director.rw2.api.client.gui.misc.GuiAlign;
import adv_director.rw2.api.client.gui.misc.GuiPadding;
import adv_director.rw2.api.client.gui.misc.GuiTransform;
import adv_director.rw2.api.client.gui.misc.IGuiRect;
import adv_director.rw2.api.client.gui.panels.IGuiCanvas;
import adv_director.rw2.api.client.gui.panels.IGuiPanel;

public class CanvasHList implements IGuiCanvas
{
	private final List<IGuiPanel> guiPanels = new ArrayList<IGuiPanel>();
	private final IGuiRect transform;
	
	public CanvasHList(IGuiRect rect)
	{
		this.transform = rect;
	}
	
	@Override
	public IGuiRect getTransform()
	{
		return transform;
	}
	
	@Override
	public void initPanel()
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		
		for(IGuiPanel entry : tmp)
		{
			entry.initPanel();
		}
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		
		for(IGuiPanel entry : tmp)
		{
			entry.drawPanel(mx, my, partialTick);
		}
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		Collections.reverse(tmp);
		boolean used = false;
		
		for(IGuiPanel entry : tmp)
		{
			used = entry.onMouseClick(mx, my, click);
			
			if(used)
			{
				break;
			}
		}
		
		return used;
	}
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		Collections.reverse(tmp);
		boolean used = false;
		
		for(IGuiPanel entry : tmp)
		{
			used = entry.onMouseScroll(mx, my, scroll);
			
			if(used)
			{
				break;
			}
		}
		
		return used;
	}
	
	@Override
	public void onKeyTyped(char c, int keycode)
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		
		for(IGuiPanel entry : tmp)
		{
			entry.onKeyTyped(c, keycode);
		}
	}
	
	@Override
	public void onPanelEvent(PanelEvent event)
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		
		for(IGuiPanel entry : tmp)
		{
			entry.onPanelEvent(event);
		}
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		List<IGuiPanel> tmp = new ArrayList<IGuiPanel>(guiPanels);
		Collections.reverse(tmp);
		
		for(IGuiPanel entry : tmp)
		{
			List<String> tt = entry.getTooltip(mx, my);
			
			if(tt != null && tt.size() > 0)
			{
				return tt;
			}
		}
		
		return new ArrayList<String>();
	}
	
	@Override
	public void addPanel(IGuiPanel panel)
	{
		if(panel == null || guiPanels.contains(panel))
		{
			return;
		}
		
		IGuiRect nt = new GuiTransform(GuiAlign.LEFT_EDGE, new GuiPadding(0, 0, 0, 0), panel.getTransform().getDepth());
		
		if(guiPanels.size() > 0)
		{
			IGuiPanel pe = guiPanels.get(guiPanels.size() - 1);
			
			int hWidth = (pe.getTransform().getX() - transform.getX()) + pe.getTransform().getWidth();
			nt = new GuiTransform(GuiAlign.LEFT_EDGE, new GuiPadding(hWidth, 0, -hWidth, 0), panel.getTransform().getDepth());
		}
		
		panel.getTransform().setParent(nt);
		
		guiPanels.add(panel);
		Collections.sort(guiPanels, ComparatorGuiDepth.INSTANCE);
	}
	
	@Override
	public boolean removePanel(IGuiPanel panel)
	{
		return guiPanels.remove(panel);
		
		// TODO: Update relative transforms
	}
	
	@Override
	public List<IGuiPanel> getAllPanels()
	{
		return guiPanels;
	}
}