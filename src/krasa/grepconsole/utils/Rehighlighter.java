package krasa.grepconsole.utils;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.impl.EditorHyperlinkSupport;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.filter.HighlightingFilter;
import krasa.grepconsole.plugin.ReflectionUtils;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.stats.StatisticsManager;

public class Rehighlighter {

	public void resetHighlights(ConsoleView console) {
		if (console instanceof ConsoleViewImpl) {
			reset((ConsoleViewImpl) console);
		} else if (console instanceof BaseTestsOutputConsoleView) {
			BaseTestsOutputConsoleView view = (BaseTestsOutputConsoleView) console;
			resetHighlights(view.getConsole());
		}
	}


	private void reset(ConsoleViewImpl consoleViewImpl) {
		Editor editor = consoleViewImpl.getEditor();
		if (editor != null) {//disposed are null - may be bug
			removeAllHighlighters(editor);
			highlightAll(consoleViewImpl, editor);
			StatisticsManager.resetStatisticsPanels(consoleViewImpl);
		}
	}

	public void removeAllHighlighters(Editor editor) {
		if (editor != null) {
			editor.getMarkupModel().removeAllHighlighters();
		}
	}

	private void highlightAll(ConsoleViewImpl consoleViewImpl, Editor editor) {
		try {
			Filter myCustomFilter = (Filter) ReflectionUtils.getPropertyValue(consoleViewImpl, "myFilters");
			if (myCustomFilter != null) {
				int lineCount = editor.getDocument().getLineCount();
				if (lineCount > 0) {
					consoleViewImpl.getHyperlinks().highlightHyperlinks(myCustomFilter, 0, lineCount - 1);
				}
			}
		} catch (NoSuchFieldException e1) {
			throw new RuntimeException("IJ API was probably changed, update the plugin or report it", e1);
		}
	}


	public void highlight(Editor editor, Project project) {
		EditorHyperlinkSupport myHyperlinks = new EditorHyperlinkSupport(editor, project);
		int lineCount = editor.getDocument().getLineCount();
		if (lineCount > 0) {
			myHyperlinks.highlightHyperlinks(getGrepFilter(project), 0, lineCount - 1);
		}
	}

	private HighlightingFilter getGrepFilter(Project project) {
		return ServiceManager.getInstance().createHighlightFilter(project, null);
	}


}
