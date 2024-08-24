package javax.microedition.lcdui;

import emulator.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class TextBox extends Screen {
//	private TextField textField;

	private SwtModifyListener swtModifyListener = new SwtModifyListener();

	private TextWrapper textWrapper;

	private int numLines;
	private boolean firstDisplayable = false;

	public TextBox(String title, String text, int maxSize, int constraints) {
		super(title);
		textWrapper = new TextWrapper(text, maxSize, constraints);
		constructSwt();
	}

	protected Composite constructSwtContent(int style) {
		Composite c = super.constructSwtContent(style);
		textWrapper.swtConstruct(c, SWT.V_SCROLL);
		return c;
	}

	public void swtShown() {
		super.swtShown();
		textWrapper.setModifyListener(swtModifyListener);
	}

	public void swtHidden() {
		super.swtHidden();
		textWrapper.setModifyListener(null);
	}

	public void swtResized(int w, int h) {
		super.swtResized(w, h);
		textWrapper.setBounds(swtContent.getClientArea());
	}

	protected void focusCaret() {
	}

	/**
	 * Get current caret position.
	 *
	 * @return current caret position
	 */
	public int getCaretPosition()
	{
		return textWrapper.getCaretPosition();
	}

	/**
	 * Returns String with the content of TextBox.
	 *
	 * @return String with TexBox content
	 */
	public String getString()
	{
		return textWrapper.getContent();
	}

	/**
	 * Set new text into TextBox. Old content is substituted with newTxt
	 *
	 * @param newText - String to set into TextBox
	 */
	public void setString(String newText)
	{
		textWrapper.setContent(newText);
	}

	/**
	 * Copies TextBox content into char[] charData.
	 *
	 * @param charData array where to copy TextBox content
	 * @return number of copied characters.
	 */
	public int getChars(char[] charData)
	{
		if(charData == null)
		{
			throw new NullPointerException();
		}
		if(charData.length < getString().length())
		{
			throw new ArrayIndexOutOfBoundsException();
		}
		String content = textWrapper.getContent();
		content.getChars(0, content.length(), charData, 0);
		return content.length();
	}

	/**
	 * Set data from char[] array into TextBox. Previous content from TextBox is
	 * substituted. Behavior is quite the same as TextBox.SetString().
	 *
	 * @param charData array of chars from where to copy.
	 * @param offset start index in charData.
	 * @param length how many characters to copy.
	 */
	public void setChars(char[] charData, int offset, int length)
	{
		String extractedString = null;
		if(charData != null)
		{
			try
			{
				extractedString = new String(charData, offset, length);
			}
			catch(IndexOutOfBoundsException e)
			{
				throw new ArrayIndexOutOfBoundsException();
			}
		}
		textWrapper.setContent(extractedString);
	}

	/**
	 * Inserts text into specified position.
	 *
	 * @param text text to insert, must not be null.
	 * @param position where to insert.
	 */
	public void insert(String text, int position)
	{
		textWrapper.insert(text, position);
	}

	/**
	 * Inserts into TextBox range of characters from []charData array.
	 *
	 * @param charData array of chars to copy from.
	 * @param offset start index in array to copy from.
	 * @param length number of characters to copy.
	 * @param position in TextBox where to insert.
	 */
	public void insert(char[] charData, int offset, int length, int position)
	{
		if(charData == null)
		{
			throw new NullPointerException();
		}
		String extractedString = null;
		try
		{
			extractedString = new String(charData, offset, length);
		}
		catch(IndexOutOfBoundsException e)
		{
			throw new ArrayIndexOutOfBoundsException();
		}
		textWrapper.insert(extractedString, position);
	}

	/**
	 * Delete range of characters from TextBox.
	 *
	 * @param offset - start index in TextBox to delete from.
	 * @param length number of characters to delete.
	 */
	public void delete(int offset, int length)
	{
		textWrapper.delete(offset, length);
	}

	/**
	 * get number if characters TextBox can contain.
	 *
	 * @return number of characters allowed for the TextBox.
	 */
	public int getMaxSize()
	{
		return textWrapper.getMaxSize();
	}

	/**
	 * Set Max number of characters. The actual maximum size
	 * may be less then newMaxSize due to platform limitations.
	 *
	 * @param newMaxSize sets the capacity of TextBox.
	 * @return maxSize that was set.
	 */
	public int setMaxSize(int newMaxSize)
	{
		textWrapper.setMaxSize(newMaxSize);
		return textWrapper.getMaxSize();
	}

	/**
	 * Get number of characters in the TextBox.
	 *
	 * @return number if inputed Characters.
	 */
	public int size()
	{
		return textWrapper.getSize();
	}

	/**
	 * Set constraint for the TextBox.
	 *
	 * @param newConstraints constraint to apply to TextBox
	 */
	public void setConstraints(int newConstraints)
	{
		textWrapper.setConstraints(newConstraints);

		if(!textWrapper.isValidText(getString() , textWrapper.getTypeConstraint(newConstraints)))
			setString("");
	}

	/**
	 * Get Current applied constraints.
	 *
	 * @return current applied constraints
	 */
	public int getConstraints()
	{
		return textWrapper.getConstraints();
	}

	/**
	 * Set initial input mode.
	 *
	 * @param inputMode input mode to set.
	 */
	public void setInitialInputMode(String inputMode)
	{
		textWrapper.setInputMode(inputMode);
	}

	/**
	 * Text modify listener.
	 */
	class SwtModifyListener implements ModifyListener
	{

		public void modifyText(ModifyEvent me)
		{
			int lines = TextWrapper.swtGetLineCount((Control) me.widget);
			if(numLines != lines)
			{
				// the number of lines changed
				numLines = lines;
//				swtSetPreferredContentSize(-1, textWrapper
//						.getPreferredHeight(Config.TEXTBOX_MAX_VISIBLE_LINES));
			}
		}

	}

	protected void paint(final Graphics graphics) {
	}

	protected void layout() {
	}

	protected void defocus() {
	}

}
