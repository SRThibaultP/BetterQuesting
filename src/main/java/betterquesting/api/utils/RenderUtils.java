package betterquesting.api.utils;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.core.BetterQuesting;

// TODO: Move text related stuff to its own utility class
@SideOnly(Side.CLIENT)
public class RenderUtils
{
	public static final String REGEX_NUMBER = "[^\\.0123456789-]"; // I keep screwing this up so now it's reusable
	
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text)
	{
		RenderItemStack(mc, stack, x, y, text, Color.WHITE.getRGB());
	}
	
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text, Color color)
	{
		RenderItemStack(mc, stack, x, y, text, color.getRGB());
	}
	
	public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text, int color)
	{
		if(stack == null)
		{
			return;
		}
		
		ItemStack rStack = stack;
		
		if(stack.getItemDamage() == OreDictionary.WILDCARD_VALUE)
		{
			NonNullList<ItemStack> tmp = NonNullList.create();
			
			stack.getItem().getSubItems(CreativeTabs.SEARCH, tmp);
			
			if(tmp.size() > 0)
			{
				rStack = tmp.get((int)((Minecraft.getSystemTime()/1000)%tmp.size()));
			}
		}
		
		GlStateManager.pushMatrix();
		RenderItem itemRender = mc.getRenderItem();
	    float preZ = itemRender.zLevel;
        
		try
		{
			float r = (float)(color >> 16 & 255) / 255.0F;
			float g = (float)(color >> 8 & 255) / 255.0F;
			float b = (float)(color & 255) / 255.0F;
		    GlStateManager.color(r, g, b);
			RenderHelper.enableGUIStandardItemLighting();
		    GlStateManager.enableRescaleNormal();
			
		    GlStateManager.translate(0.0F, 0.0F, 32.0F);
		    itemRender.zLevel = 200.0F;
		    FontRenderer font = rStack.getItem().getFontRenderer(rStack);
		    if (font == null) font = mc.fontRenderer;
		    itemRender.renderItemAndEffectIntoGUI(rStack, x, y);
		    itemRender.renderItemOverlayIntoGUI(font, rStack, x, y, text);
		    
		    RenderHelper.disableStandardItemLighting();
		} catch(Exception e)
		{
			BetterQuesting.logger.warn("Unabled to render item " + stack);
		}
		
	    itemRender.zLevel = preZ; // Just in case
		
        GlStateManager.popMatrix();
	}

    public static void RenderEntity(int posX, int posY, int scale, float rotation, float pitch, Entity entity)
    {
    	try
    	{
	        GlStateManager.enableColorMaterial();
	        GlStateManager.pushMatrix();
	        GlStateManager.enableDepth();
	        GlStateManager.translate((float)posX, (float)posY, 100.0F);
	        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
	        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
	        GlStateManager.rotate(pitch, 1F, 0F, 0F);
	        GlStateManager.rotate(rotation, 0F, 1F, 0F);
	        float f3 = entity.rotationYaw;
	        float f4 = entity.rotationPitch;
	        RenderHelper.enableStandardItemLighting();
	        GlStateManager.translate(0D, entity.getYOffset(), 0D);
	        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
	        rendermanager.setPlayerViewY(180.0F);
	        rendermanager.doRenderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
	        entity.rotationYaw = f3;
	        entity.rotationPitch = f4;
	        GlStateManager.popMatrix();
	        RenderHelper.disableStandardItemLighting();
	        GlStateManager.disableRescaleNormal();
	        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
	        GlStateManager.disableTexture2D();
	        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    	} catch(Exception e)
    	{
    		// Hides rendering errors with entities which are common for invalid/technical entities
    	}
    }
	
	public static void DrawLine(int x1, int y1, int x2, int y2, float width, int color)
	{
		float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
		GlStateManager.pushMatrix();
		
		GlStateManager.disableTexture2D();
		GlStateManager.color(r, g, b, 1F);
		GL11.glLineWidth(width);
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(x1, y1);
		GL11.glVertex2f(x2, y2);
		GL11.glEnd();
		
		GlStateManager.enableTexture2D();
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		GlStateManager.popMatrix();
	}
	
	public static void drawSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow)
	{
		drawSplitString(renderer, string, x, y, width, color, shadow, 0, renderer.listFormattedStringToWidth(string, width).size() - 1);
	}
	
	public static void drawSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow, int start, int end)
	{
		drawHighlightedSplitString(renderer, string, x, y, width, color, shadow, start, end, 0, 0, 0);
	}
	
	// TODO: Clean this up. The list of parameters is getting a bit excessive
	
	public static void drawHighlightedSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow, int highlightColor, int highlightStart, int highlightEnd)
	{
		drawHighlightedSplitString(renderer, string, x, y, width, color, shadow, 0, renderer.listFormattedStringToWidth(string, width).size() - 1, highlightColor, highlightStart, highlightEnd);
	}
	
	public static void drawHighlightedSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow, int start, int end, int highlightColor, int highlightStart, int highlightEnd)
	{
		if(renderer == null || string == null || string.length() <= 0 || start > end)
		{
			return;
		}
		
		string = string.replaceAll("\r", ""); //Line endings from localizations break things so we remove them
		
		List<String> list = renderer.listFormattedStringToWidth(string, width);
		List<String> noFormat = splitStringWithoutFormat(string, width, renderer); // Needed for accurate highlight index positions
		
		int hlStart = Math.min(highlightStart, highlightEnd);
		int hlEnd = Math.max(highlightStart, highlightEnd);
		int idxStart = 0;
		
		for(int i = 0; i < start; i++)
		{
			idxStart += noFormat.get(i).length();
		}
		
		for(int i = start; i <= end; i++)
		{
			if(i < 0 || i >= list.size())
			{
				continue;
			}
			
			renderer.drawString(list.get(i), x, y + (renderer.FONT_HEIGHT * (i - start)), color, shadow);
			
			int lineSize = noFormat.get(i).length();
			int idxEnd = idxStart + lineSize;
			
			if((idxStart >= hlStart && idxStart <= hlEnd) || (idxEnd >= hlStart && idxEnd <= hlEnd))
			{
				int i1 = Math.max(idxStart, highlightStart);
				int i2 = Math.min(idxEnd, highlightEnd);
				
				if(i1 != i2)
				{
					int x1 = renderer.getStringWidth(noFormat.get(i).substring(0, i1));
					int x2 = renderer.getStringWidth(noFormat.get(i).substring(0, i2));
					
					drawHighlightBox(x + x1, y + (renderer.FONT_HEIGHT * (i - start)), x + x2, y + (renderer.FONT_HEIGHT * (i - start)) + renderer.FONT_HEIGHT, highlightColor);
				}
			}
			
			idxStart = idxEnd;
		}
	}
	
	public static void drawHighlightedString(FontRenderer renderer, String string, int x, int y, int color, boolean shadow, int highlightColor, int highlightStart, int highlightEnd)
	{
		if(renderer == null || string == null || string.length() <= 0)
		{
			return;
		}
		
		renderer.drawString(string, x, y, color, shadow);
		
		int idxEnd = string.length();
		
		int i1 = Math.max(0, highlightStart);
		int i2 = Math.min(idxEnd, highlightEnd);
		
		if(i1 != i2)
		{
			int x1 = renderer.getStringWidth(string.substring(0, i1));
			int x2 = renderer.getStringWidth(string.substring(0, i2));
			
			drawHighlightBox(x + x1, y, x + x2, y + renderer.FONT_HEIGHT, highlightColor);
		}
	}
	
	public static void drawHighlightBox(int left, int top, int right, int bottom, int color)
	{
        if (left < right)
        {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
		
		GlStateManager.pushMatrix();
  
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(f, f1, f2, f3);
        GlStateManager.disableTexture2D(); // TODO: Figure out why texture 2D causes the first attempt to fail
       	GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos((double)left, (double)bottom, 0.0D).endVertex();
        bufferbuilder.pos((double)right, (double)bottom, 0.0D).endVertex();
        bufferbuilder.pos((double)right, (double)top, 0.0D).endVertex();
        bufferbuilder.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
        
		GL11.glEnable(GL11.GL_TEXTURE_2D);
        
        GlStateManager.popMatrix();
	}
	
	/**
	 * Performs a OpenGL scissor based on Minecraft's resolution instead of display resolution
	 */
	@Deprecated
	public static void guiScissor(Minecraft mc, int x, int y, int w, int h)
	{
		ScaledResolution r = new ScaledResolution(mc);
		int f = r.getScaleFactor();
		
		GL11.glScissor(x * f, (r.getScaledHeight() - y - h)*f, w * f, h * f);
	}
	
	private static Stack<IGuiRect> scissorStack = new Stack<>();
	
	/**
	 * Performs a OpenGL scissor based on Minecraft's resolution instead of display resolution and adds it to the stack of ongoing scissors.
	 * Not using this method will result in incorrect scissoring and scaling of parent/child GUIs
	 */
	public static void startScissor(Minecraft mc, GuiRectangle rect)
	{
		if(scissorStack.size() >= 100)
		{
			BetterQuesting.logger.log(Level.ERROR, "More than 100 recursive scissor calls have been made!");
			return;
		}
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		ScaledResolution r = new ScaledResolution(mc);
		int f = r.getScaleFactor();
		
		// Have to do all this fancy stuff because glScissor() isn't affected by glScale() or glTranslate() and rather than try and convince devs to use some custom hack
		// we'll just deal with it by reading from the current MODELVIEW MATRIX to convert between screen spaces at their relative scales and translations.
		FloatBuffer fb = BufferUtils.createFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, fb);
		fb.rewind();
		Matrix4f fm = new Matrix4f();
		fm.load(fb);
		
		// GL screenspace rectangle
		GuiRectangle sRect = new GuiRectangle((int)(rect.getX() * f  * fm.m00 + (fm.m30 * f)), (r.getScaledHeight() - (int)((rect.getY() + rect.getHeight()) * fm.m11 + fm.m31)) * f, (int)(rect.getWidth() * f * fm.m00), (int)(rect.getHeight() * f * fm.m11));
		
		if(!scissorStack.empty())
		{
			IGuiRect parentRect = scissorStack.peek();
			int x = Math.max(parentRect.getX(), sRect.getX());
			int y = Math.max(parentRect.getY(), sRect.getY());
			int w = Math.min(parentRect.getX() + parentRect.getWidth(), sRect.getX() + sRect.getWidth());
			int h = Math.min(parentRect.getY() + parentRect.getHeight(), sRect.getY() + sRect.getHeight());
			w = Math.max(0, w - x); // Clamp to 0 to prevent OpenGL errors
			h = Math.max(0, h - y); // Clamp to 0 to prevent OpenGL errors
			sRect = new GuiRectangle(x, y, w, h, 0);
		}
		
		GL11.glScissor(sRect.getX(),sRect.getY(), sRect.getWidth(), sRect.getHeight());
		scissorStack.add(sRect);
	}
	
	/**
	 * Pops the last scissor off the stack and returns to the last parent scissor or disables it if there are none
	 */
	public static void endScissor()
	{
		scissorStack.pop();
		
		if(scissorStack.empty())
		{
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		} else
		{
			IGuiRect rect = scissorStack.peek();
			GL11.glScissor(rect.getX(),rect.getY(), rect.getWidth(), rect.getHeight());
		}
	}
	
	/**
	 * Similar to normally splitting a string with the fontRenderer however this variant does
	 * not attempt to preserve the formatting between lines. This is particularly important when the
	 * index positions in the text are required to match the original unwrapped text.
	 */
	public static List<String> splitStringWithoutFormat(String str, int wrapWidth, FontRenderer font)
	{
		List<String> list = new ArrayList<>();
		
		String lastFormat = ""; // Formatting like bold can affect the wrapping width
		
		String[] nlSplit = str.split("\n");
		
		for(int i = 0; i < nlSplit.length; i++)
		{
			String s = nlSplit[i] + (i + 1 < nlSplit.length? "\n" : ""); // Preserve new line characters for indexing accuracy
			
			while(font.getStringWidth(lastFormat + s) >= wrapWidth) // Continue until wrapping is unnecessary
			{
				// Formatting can affect the width so we still have to split as if it were there
				lastFormat = FontRenderer.getFormatFromString(lastFormat + s);
				int n = sizeStringToWidth(lastFormat + s, wrapWidth, font); // How many characters will fit into width with formatting
				n -= lastFormat.length(); // Remove formatting characters from count
				n = Math.max(1, n); // Line must be at least 1 character long
				String subTxt = s.substring(0, n); // Cut out our measured substring from the original line
				list.add(subTxt); // Append substring to list
				s = s.replaceFirst(Pattern.quote(subTxt), ""); // Remove our substring from original line and prep for the next
			}
			
			list.add(s); // Add whatever is leftover
		}
        
        return list;
	}
	
	/**
	 * Returns the index position under a given set of coordinates in a piece of text
	 */
	public static int getCursorPos(String text, int x, FontRenderer font)
	{
		int i = 0;
		int swl = 0;
		int swc;
		
		for(; i < text.length(); i++)
		{
			swc = font.getStringWidth(text.substring(0, i + 1));
			
			if(swc > x)
			{
				if(Math.abs(x - swl) >= Math.abs(swc - x))
				{
					i++;
				}
				
				break;
			} else
			{
				swl = swc;
			}
		}
		
		return i;
	}
	
	/**
	 * Returns the index position under a given set of coordinates in a wrapped piece of text
	 */
	public static int getCursorPos(String text, int x, int y, int width, FontRenderer font)
	{
		List<String> tLines = RenderUtils.splitStringWithoutFormat(text, width, font);
		
		if(tLines.size() <= 0)
		{
			return 0;
		}
		
		int row = MathHelper.clamp(y/font.FONT_HEIGHT, 0, tLines.size() - 1);
		String lastFormat = "";
		int idx = 0;
		
		for(int i = 0; i < row; i++)
		{
			String line = tLines.get(i);
			idx += line.length();
			lastFormat = FontRenderer.getFormatFromString(lastFormat + line);
		}
		
		return idx + getCursorPos(tLines.get(row), x, font);
	}
	
    private static int sizeStringToWidth(String str, int wrapWidth, FontRenderer font)
    {
        int i = str.length();
        int j = 0;
        int k = 0;
        int l = -1;

        for (boolean flag = false; k < i; ++k)
        {
            char c0 = str.charAt(k);

            switch (c0)
            {
                case '\n':
                    --k;
                    break;
                case ' ':
                    l = k;
                default:
                    j += font.getCharWidth(c0);

                    if (flag)
                    {
                        ++j;
                    }

                    break;
                case '\u00a7':

                    if (k < i - 1)
                    {
                        ++k;
                        char c1 = str.charAt(k);

                        if (c1 != 108 && c1 != 76)
                        {
                            if (c1 == 114 || c1 == 82 || isFormatColor(c1))
                            {
                                flag = false;
                            }
                        }
                        else
                        {
                            flag = true;
                        }
                    }
            }

            if (c0 == 10)
            {
                ++k;
                l = k;
                break;
            }

            if (j > wrapWidth)
            {
                break;
            }
        }

        return k != i && l != -1 && l < k ? l : k;
    }
    
    private static boolean isFormatColor(char colorChar)
    {
        return colorChar >= 48 && colorChar <= 57 || colorChar >= 97 && colorChar <= 102 || colorChar >= 65 && colorChar <= 70;
    }
    
	public static float lerpFloat(float f1, float f2, float blend)
	{
		return (f2 * blend) + (f1 * (1F - blend));
	}
	
	public static int lerpRGB(int c1, int c2, float blend)
	{
		float a1 = c1 >> 24 & 255;
		float r1 = c1 >> 16 & 255;
		float g1 = c1 >> 8 & 255;
		float b1 = c1 & 255;
		
		float a2 = c2 >> 24 & 255;
		float r2 = c2 >> 16 & 255;
		float g2 = c2 >> 8 & 255;
		float b2 = c2 & 255;
		
		int a3 = (int)lerpFloat(a1, a2, blend);
		int r3 = (int)lerpFloat(r1, r2, blend);
		int g3 = (int)lerpFloat(g1, g2, blend);
		int b3 = (int)lerpFloat(b1, b2, blend);
		
		return (a3 << 24) + (r3 << 16) + (g3 << 8) + b3;
	}
}
