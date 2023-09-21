package com.yabaipanda.mangaupdater.chapter;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.yabaipanda.mangaupdater.R;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Chapter {
	public static final int DATA_LENGTH = 13;
	private static final int loop_count = 10;
	private static final String DELIMITER = "|";
	public final int LIKE = 1, LOVE = 2, HIATUS = 4, COMPLETE = 8, POSSIBLE = 16, USE_JS = 32, USE_DEFAULT = 64;
	Connection conn;
	private String prevURL, nextURL, title, sourceURL, selector, prevChapNum, nextChapNum;
	private boolean loaded;
	private int linkPos, snoozeDelay, state, errorCode = 0;
	private LocalDate snoozeDate, lastReadDate, newUpdatedDate, unsnoozedDate, lastUpdatedDate;
	public volatile WebView jsView = null;
	private volatile boolean isJSPageLoaded;
	private volatile Document jsDoc;

	public Chapter (String input) {
		loadData(input);
		loaded = false;
	}

	public static String capitalize (final String words) {
		return Stream.of(words.replaceAll("-", " ").trim().split("\\s")).filter(word -> word.length() > 0).map(word -> word.substring(0, 1).toUpperCase() + word.substring(1)).collect(Collectors.joining(" "));
	}
	private static LocalDate toLocal (Date date) {
		if (date == null) return null;
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	private static Date revertToDate (LocalDate date) {
		if (date == null) return null;
		return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	private static String dateToStr (LocalDate date) {
		if (date == null) return "";
		return date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
	}
	public CompletableFuture<String> findSelectorAsync (String bURL, String firstURL, String secondURL) {
		return CompletableFuture.supplyAsync(() -> {
			if (firstURL.equals(secondURL)) return "";
			String tempBUSRL = sourceURL;
			sourceURL = bURL;
			Document doc = getDoc();
			sourceURL = tempBUSRL;
			if (doc == null) return "";
			String firstCSSQuery = "a[href$='" + firstURL.substring(firstURL.lastIndexOf('/')) + "']";
			String secondCSSQuery = "a[href$='" + secondURL.substring(secondURL.lastIndexOf('/')) + "']";
			Elements prevLinks = doc.select(firstCSSQuery);
			Elements searchLinks = doc.select(secondCSSQuery);
			Element[] links = findNearestParent(prevLinks, searchLinks, firstCSSQuery, secondCSSQuery);
			return getSelectorString(doc, links);
		}, Executors.newCachedThreadPool());
	}
	private Document getDoc (){
		if (isState(USE_JS)){
			if (jsView != null) {
				long startTime = System.currentTimeMillis();
				long timeout = 15000; // Timeout duration in milliseconds (adjust as needed)
				while (!isJSPageLoaded && System.currentTimeMillis() - startTime < timeout);
				if(!isJSPageLoaded) return null;
				jsDoc = null;
				jsView.getContext().getMainExecutor().execute(() ->
						jsView.loadUrl("javascript:window.HTMLChapterViewer.setJSFin"+
								"('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');"));
				startTime = System.currentTimeMillis();
				while (jsDoc == null && System.currentTimeMillis() - startTime < timeout);
				if (jsDoc != null) Log.d("Use JS", "Got doc for: " + getTitle());
				return jsDoc;
			} else Log.d("Use JS", "No WebView found.....");
		} else for (int i = 0; i < loop_count; i++)
			try {
				if (conn == null) conn = Jsoup.connect(getOptSourceUrl());
				return conn.timeout(15000).userAgent("Mozilla").get();
			} catch (Exception e) {
				if (e instanceof SocketTimeoutException) break;
				else if (i == loop_count - 1) {
					if (e instanceof HttpStatusException) {
						errorCode = ((HttpStatusException) e).getStatusCode();
						Log.e("UpdateAsync", errorCode + " Error for " + title);
					} else e.printStackTrace();
				}
			}
		return null;
	}
	@JavascriptInterface
	public void setJSFin(String str) { jsDoc = Jsoup.parse(str);}
	static Elements recursiveSelect (Document doc, String selector) {
		Elements e = doc.getAllElements();
		for (String s : selector.split("&")) {
			e = e.select(s);
		}
		return e;
	}
	private static String getSelectorString (Document doc, Element[] links) {
		if (links[0] == null) return "";
		// Get the selector path to find the links within the nearest parent element
		StringBuilder sb = new StringBuilder();
		for (Element p : links[0].parents()) {
			StringBuilder my_sb = new StringBuilder();
			String[] attrs = {p.tagName(), p.className().isEmpty() ? "" : "." + p.className(), p.id().isEmpty() ? "" : "#" + p.id()};
			for (String attr : attrs)
				if (!attr.isEmpty() && recursiveSelect(doc, sb.toString() + my_sb + attr).select("a[href]").contains(links[1]) && recursiveSelect(doc, sb.toString() + my_sb + attr).select("a[href]").contains(links[2]))
					my_sb.append(attr);
			if (!my_sb.toString().isEmpty() && !my_sb.toString().equals(p.tagName()))
				sb.append(my_sb).append("&");
		}
		return sb + links[0].tagName() + (links[0].id().isEmpty() ? "" : "#" + links[0].id()) + (links[0].className().isEmpty() ? "" : "." + links[0].className());
	}
	private static Element[] findNearestParent (Elements prevLinks, Elements searchLinks, String firstCSSQuery, String secondCSSQuery) {
		Element[] links = {null, null, null};
		int closestDistance = Integer.MAX_VALUE;

		for (Element prevLink : prevLinks) {
			for (Element searchLink : searchLinks) {
				// Check if the links have the same number of ancestors
				if (prevLink.parents().size() != searchLink.parents().size()) continue;

				Element parent = prevLink.parents().stream().filter(searchLink.parents()::contains).findFirst().orElse(null);
				if (parent != null) {
					// Calculate the distance from the common parent element to the two links
					int distance = parent.select(firstCSSQuery).first().siblingIndex() - parent.select(secondCSSQuery).first().siblingIndex();
					distance = distance < 0 ? -distance : distance;

					if (distance < closestDistance) {
						// Found a closer common parent element
						links = new Element[]{parent, prevLink, searchLink};
						closestDistance = distance;
					}
				}
			}
		}

		return links;
	}
	public String saveData () {
		StringBuilder saveStr = new StringBuilder();
		Object[] saveDate = new Object[]{clean(title), clean(sourceURL), clean(prevURL), clean(nextURL), clean(selector), state, linkPos, snoozeDelay, dateToStr(snoozeDate), dateToStr(lastReadDate), dateToStr(newUpdatedDate), dateToStr(lastUpdatedDate), dateToStr(unsnoozedDate)};
		for (int i = 0; i < DATA_LENGTH; i++)
			saveStr.append(String.format("~%s" + DELIMITER, saveDate[i]));
		return "~V#3|" + saveStr;
	}
	public void loadData (String input){
		int versionNum = 0;
		if (input.startsWith("~V#3|")) {
			input = input.replace("~V#3|", "");
			versionNum = 3;
		}
		String[] substrings = input.split("\\" + DELIMITER);
		String[] output = new String[DATA_LENGTH];
		Arrays.fill(output, "");
		for (int i = 0; i < substrings.length; i++)
			output[i] = substrings[i].trim().replace("~", "");
		if (versionNum == 3) {
			title = output[0];
			sourceURL = output[1];
			prevURL = output[2];
			nextURL = output[3];
			selector = output[4];
			state = Parser.Int(output[5]);
			linkPos = Parser.Int(output[6], 1);
			snoozeDelay = Parser.Int(output[7]);
			snoozeDate = Parser.Date(output[8]);
		} else {
			prevURL = output[0];
			sourceURL = output[1];
			nextURL = output[2];
			title = output[3];
			selector = output[4];
			snoozeDelay = Parser.Int(output[5]);
			snoozeDate = Parser.Date(output[6]);
			linkPos = Parser.Int(output[7], 1);
			state = Parser.Int(output[8]);
		}

		lastReadDate = Parser.Date(output[9]);
		newUpdatedDate =  Parser.Date(output[10]);
		lastUpdatedDate = Parser.Date(output[11]);
		unsnoozedDate = Parser.Date(output[12]);

		prevChapNum = getChapterNumber(prevURL);
		nextChapNum = getChapterNumber(nextURL);
	}
	private String clean (String string) {
		return string.replace(DELIMITER, "").trim();
	}
	private String getChapterNumber (String url) {
		String[] urlParts = url.split("/");
		return urlParts[urlParts.length - 1].substring(urlParts[urlParts.length - 1].indexOf("-") + 1).replaceAll("-", ".");
	}
	public boolean GetUpdate () {
		loaded = false;
		if (sourceURL.isEmpty()) {
			try {
				sourceURL = prevURL;
				if (sourceURL.endsWith("/")) sourceURL = sourceURL.substring(0, sourceURL.length() - 1);
				sourceURL = sourceURL.substring(0, sourceURL.lastIndexOf("/") + 1);
			} catch (Exception e) {
				sourceURL = "";
				return false;
			}
		}
		if (title.isEmpty()) {
			String[] urlParts = prevURL.split("/");
			title = capitalize(urlParts[urlParts.length - 2]).replaceAll("[0-9]*", "");
		}
		if (linkPos == 0) linkPos = 1;
		try {
			Document doc = getDoc();
			if (doc == null) return false;
			Elements chapterLinks;
			if (selector.isEmpty()) chapterLinks = CheckDefaultSelectors(doc);
			else chapterLinks = recursiveSelect(doc, selector).select("a[href]");
			if (chapterLinks.isEmpty()) {
				if (sourceURL.equalsIgnoreCase("NONE")) loaded = true;
				else errorCode = -123;
				return false;
			}
			if (sourceURL.equalsIgnoreCase("NONE"))
				Log.d("WOWOOWOW", "Oops");
			Element lastChap = chapterLinks.get(((linkPos < 0) ? chapterLinks.size() : -1) + linkPos);
			Uri hostURI = Uri.parse(lastChap.baseUri().equalsIgnoreCase("") ? getOptSourceUrl() : lastChap.baseUri());
			lastChap.setBaseUri("https://" + hostURI.getHost());
			String prevNext = nextURL;
			nextURL = lastChap.absUrl("href");
			nextChapNum = getChapterNumber(nextURL);
			loaded = true;
			UpdateDelays();
			if (Objects.equals(prevNext, prevURL) && !Objects.equals(prevURL, nextURL))
				newUpdatedDate = LocalDate.now();
			boolean updated = !prevURL.equals(nextURL);
			if (updated) lastUpdatedDate = LocalDate.now();
			return updated;
		} catch (IndexOutOfBoundsException ex) {
			errorCode = -456;
		} catch (Exception ex) {
			Log.e("UpdateAsync", "Update for " + title);
			ex.printStackTrace();
		}
		loaded = false;
		return false;
	}

	@NonNull
	private Elements CheckDefaultSelectors (Document document) {
		Elements chapterLinks;
		chapterLinks = document.select(selector = ".wp-manga-chapter").select("a[href]");
		if (chapterLinks.isEmpty())
			chapterLinks = document.select(selector = ".chapter-list li").select("a[href]");
		if (chapterLinks.isEmpty())
			chapterLinks = document.select(selector = "div#chapter-list-inner").select("a[href]");
		if (chapterLinks.isEmpty())
			chapterLinks = recursiveSelect(document, selector = "div#chapter-list-inner&ul#chapter-list.chapter-list").select("a[href]");
		if (chapterLinks.isEmpty()) {
			chapterLinks = recursiveSelect(document, selector = "astro-island&astro-slot&div.space-x-1").select("a[href]");
			if (!chapterLinks.isEmpty()) linkPos = -3;
		}
		if (chapterLinks.isEmpty()) selector = "";
		return chapterLinks;
	}
	public String getPrevURL () { return prevURL; }
	public String getNextURL () { return nextURL; }
	public String getTitle () { return title; }
	public String getSourceURL () { return sourceURL; }
	public String getSelector () { return selector; }
	public void setSelector (String selector, String sourceURL) {
		this.selector = selector;
		this.sourceURL = sourceURL;
	}
	public boolean isUnloaded () { return !loaded; }
	public boolean isUpdated () {
		return nextURL != null && !nextURL.isEmpty() && !prevURL.equals(nextURL);
	}
	public boolean isSnoozed () {
		return snoozeDelay != 0 || snoozeDate != null || isState(HIATUS);
	}
	public boolean isState (int checkedState) { return (state & checkedState) > 0; }
	public int getState () { return state; }
	public void setState (int changeState) { state = changeState; }
	public void UpdateDelays () {
		if ((!prevChapNum.isEmpty() && !nextChapNum.isEmpty()) && snoozeDelay != 0) {
			if (getChapDiff() >= snoozeDelay) {
				resetSnooze();
				return;
			}
		}
		if (isState(HIATUS) && snoozeDate == null && !freshUnsnoozed())
			snoozeDate = LocalDate.now().plusMonths(6);
		if (shouldReset(LocalDate.now())) resetSnooze();
	}
	public int getChapDiff () {
		try {
			int currChap = Integer.parseInt((prevChapNum.split("\\.")[0]).replaceAll("[^0-9]", ""));
			int nextChap = Integer.parseInt(nextChapNum.split("\\.")[0].replaceAll("[^0-9]", ""));
			return nextChap - currChap;
		} catch (Exception ignored) { return 0; }
	}
	private boolean shouldReset (LocalDate today) {
		if (snoozeDate == null) return false;
		return today.isAfter(snoozeDate) || today.isEqual(snoozeDate);
	}
	private void resetSnooze () {
		snoozeDelay = 0;
		snoozeDate = null;
		unsnoozedDate = LocalDate.now();
	}
	public void markAsRead () {
		prevURL = nextURL;
		prevChapNum = nextChapNum;
		newUpdatedDate = null;
		lastReadDate = LocalDate.now();
		ChapterManager.Refresh(ChapterManager.allChapters.indexOf(this));
		if (sourceURL.equalsIgnoreCase("NONE")) {
			loaded = false;
			if (jsView != null) {
				isJSPageLoaded = false;
				jsView.loadUrl(getOptSourceUrl());
			}
		}
	}
	public void CreateWebView(Context ctx){
		if (!isState(USE_JS)) return;
		jsView = new WebView(ctx);
		jsView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		jsView.getSettings().setJavaScriptEnabled(true);
		jsView.addJavascriptInterface(this, "HTMLChapterViewer");
		jsView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished (WebView view, String url) {
				super.onPageFinished(view, url);
				isJSPageLoaded = true;
				Log.d("Use JS", "Loaded... "+ getTitle());
			}
		});
		jsView.loadUrl(getOptSourceUrl());
	}

	public SpannableString getLinks (View v) {
		SpannableString spannableString = new SpannableString("<" + prevChapNum + "/" + nextChapNum + ">");
		ClickableSpan prevChapterLink = getChapterLink(v, prevURL, () -> { });
		ClickableSpan nextChapterLink = getChapterLink(v, nextURL, this::markAsRead);
		spannableString.setSpan(prevChapterLink, 1, prevChapNum.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannableString.setSpan(nextChapterLink, prevChapNum.length() + 2, spannableString.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannableString;
	}
	@NonNull
	private ClickableSpan getChapterLink (View v, String url, Runnable extra) {
		return new ClickableSpan() {
			@Override
			public void onClick (@NonNull View widget) {
				extra.run();
				Intent browserIntent = new Intent(Intent.ACTION_WEB_SEARCH, Uri.parse(url));
				Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
				chooserIntent.putExtra(Intent.EXTRA_TITLE, "Open with");
				chooserIntent.putExtra(Intent.EXTRA_INTENT, browserIntent);
				v.getContext().startActivity(chooserIntent);
			}
		};
	}
	public void UpdateData (String title, String sourceURL, String prevURL, String nextURL, String selector, int linkPos) {
		this.title = title;
		this.prevURL = prevURL;
		this.sourceURL = sourceURL;
		this.nextURL = nextURL;
		this.selector = selector;
		this.linkPos = linkPos;
		prevChapNum = getChapterNumber(prevURL);
		nextChapNum = getChapterNumber(nextURL);
	}
	public void UpdateDate (int delay, Date date) {
		this.snoozeDelay = delay;
		this.snoozeDate = toLocal(date);
		UpdateDelays();
	}
	public void setIcon (ImageView icon) {
		String color = (isUnloaded() ? "#FFD000" : (isSnoozed()) ? "#50D6EB" : "#808080");
		int img = (isSnoozed() ? R.drawable.ic_baseline_snooze_24 : (isUnloaded() ? R.drawable.ic_baseline_warning_24 : R.drawable.outline_circle_24));
		icon.setImageResource(img);
		icon.setColorFilter(Color.parseColor(color));
	}
	public int getSnoozeDelay () { return snoozeDelay; }
	public Date getSnoozeDate () { return revertToDate(snoozeDate); }
	public boolean isDup () { return prevChapNum.equals(nextChapNum); }
	public int getLinkPos () { return linkPos; }
	public int getErrorCode () { return errorCode; }
	public SpannableString getTitleLink (View v, int margin) {
		SpannableString spannableString = new SpannableString(getTitle() + "↪ ");
		spannableString.setSpan(getChapterLink(v, getOptSourceUrl(), () -> {
		}), spannableString.length() - 2, spannableString.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (spannableString.length() > 0)
			spannableString.setSpan(new LeadingMarginSpan.Standard(margin, 0), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannableString;
	}
	public boolean hasNewUpdate () {
		if (newUpdatedDate == null) return false;
		return !newUpdatedDate.isBefore(LocalDate.now().minusDays(1));
	}
	public boolean freshUnsnoozed () {
		if (unsnoozedDate == null) return false;
		return !unsnoozedDate.isBefore(LocalDate.now().minusDays(1));
	}
	public boolean displayUpdate(){
		return isUpdated() && !isSnoozed() && !isState(POSSIBLE);
	}

	public void flipStatus (int changeState) {
		boolean onState = (state & changeState) > 0;
		if (!onState) {
			if (changeState == LOVE || changeState == LIKE || changeState == POSSIBLE) {
				state &= ~LOVE;
				state &= ~LIKE;
				state &= ~POSSIBLE;
			}
			if (changeState == HIATUS && snoozeDate == null)
				snoozeDate = LocalDate.now().plusMonths(6);
			state |= changeState;
		} else {
			if (changeState == HIATUS) snoozeDate = null;
			state &= ~changeState;
		}
	}
	public void setState (int changeState, boolean goal) {
		if (isState(changeState) != goal) flipStatus(changeState);
	}
	public String getSnoozeInfoString () {
		StringBuilder info = new StringBuilder();
		if (snoozeDate != null)
			info.append("D").append(ChronoUnit.DAYS.between(LocalDate.now().atStartOfDay(), snoozeDate.atStartOfDay()));
		if (snoozeDelay != 0) {
			if (info.length() != 0) info.append('/');
			info.append("#").append(snoozeDelay - getChapDiff());
		}
		return info.toString();
	}

	private String getOptSourceUrl(){ return sourceURL.equalsIgnoreCase("NONE")?prevURL : sourceURL; }
}
