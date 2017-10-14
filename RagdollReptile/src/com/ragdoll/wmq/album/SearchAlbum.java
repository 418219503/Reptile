package com.ragdoll.wmq.album;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SearchAlbum {
	private static String host = "http://www.ximalaya.com/";

	private static String request = "search/album/kw/#searchWord#/page/#currentPage#/sc/true";

	/*
	 * 每一页最多20个数据
	 */
	private static final int pageMaxNum = 20;
	
	/*
	 * 搜索关键字的分页数
	 */
	private static int totalPage = 1;

	/*
	 * 当前分页页数
	 */
	private static int currentPage = 1;

	/*
	 * 搜索关键字，修改该变量可以搜索不同关键字
	 */
	private static String searchWord = "周杰伦";

	/*
	 * 搜索结果
	 */
	private static ArrayList<Album> albums = new ArrayList<Album>();

	/*
	 * 爬取网页数量
	 */
	private static int searchPageNum = 0;

	/*
	 * 获取相应请求的网页数据
	 */
	private Document getUrlContent(String url, String param) {
		if (url == null || param == null)
			return null;

		System.out.println("开始抓取网页:" + url + param);
		Document document = null;
		try {
			/*
			 * 根据抓包信息，设置HTTP HEADER信息
			 */
			document = Jsoup
					.connect(url + param)
					.header("Accept",
							"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
					.header("Accept-Encoding", "gzip, deflate,sdch")
					.header("Accept-Language",
							"zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
					.header("Referer", url + param)
					.header("User-Agent",
							"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
					.timeout(5000).get();
		} catch (IOException e) {
			System.out.println("connect error...");
		}

		if (document != null)
			searchPageNum++;

		return document;
	}

	/*
	 * 将输入的搜索词做UTF-8编码
	 */
	private String getEncodeWord(String sw) {
		String encodeWord = "";
		try {
			encodeWord = URLEncoder.encode(sw, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			System.out.println("UnsupportedEncodingException : " + sw);
		}

		return encodeWord;
	}

	/*
	 * 获取每个网页中的专辑数据
	 */
	private void getHref(Document doc) {
		if (doc == null)
			return;

		// 专辑分页数
		if(totalPage == 1)
		{
			Elements albumCount = doc.getElementsByAttributeValueMatching(
					"class", Pattern.compile("search-lists-tit c02"));
			String element = albumCount.first().toString();
			int start = element.indexOf(">(") + ">(".length();
			int end = element.indexOf(")<");
			int totalAlbumNum = Integer.parseInt(element.substring(start, end));
			totalPage = (totalAlbumNum / pageMaxNum) + ((totalAlbumNum % pageMaxNum == 0) ? 0 : 1);
			System.out.println("一共有" + totalAlbumNum + "条数据," + totalPage + "个分页,请耐心等待...");
		}
		
		// 专辑简介
		Elements introElements = doc.getElementsByAttributeValueMatching(
				"class", Pattern.compile("elli c02"));
		// 专辑播放量
		Elements playNumElements = doc.getElementsByAttributeValueMatching(
				"class", Pattern.compile("iconfont icon-playlight"));
		// 专辑名称
		Elements albumTitleElements = new Elements();
		// 专辑所属账号
		Elements albumUserElements = new Elements();

		Elements href = doc.getElementsByAttributeValueMatching("class",
				Pattern.compile("search-album-tit elli-multi link4"));

		for (Object e : href.toArray()) {
			Pattern hrefPattern = Pattern.compile("/\\d+/album/\\d+");
			Matcher hrefMatcherer = hrefPattern.matcher(e.toString());
			hrefMatcherer.find();
			String request = hrefMatcherer.group();
			// 对每一个专辑进行网页数据抓取
			Document albumDocument = getUrlContent(host, request);
			// 专辑名称
			Elements albumTitle = albumDocument
					.getElementsByAttributeValueMatching("class",
							Pattern.compile("detailContent_title"));
			albumTitleElements.add(albumTitle.first());
			// 专辑所属账号
			Elements userName = albumDocument
					.getElementsByAttributeValueMatching("class",
							Pattern.compile("^username$"));
			albumUserElements.add(userName.first());
		}

		/*
		 * System.out.println(introElements);
		 * System.out.println(playNumElements);
		 * System.out.println(albumTitleElements);
		 * System.out.println(albumUserElements);
		 */

		getAlbumArray(albumTitleElements, albumUserElements, introElements,
				playNumElements);
	}

	/*
	 * 解析获取的title,anchorman,info,totalPlayCount标签元素
	 */
	private static void getAlbumArray(Elements titles, Elements anchormans,
			Elements infos, Elements totalPlayCounts) {
		int size = titles.size();
		for (int i = 0; i < size; ++i) {
			String title = titles.get(i).text().trim();

			String anchorman = "";
			Element element = anchormans.get(i);
			int start = element.toString().indexOf("\"username\">")
					+ "\"username\">".length();
			int end1 = element.toString().indexOf("<i");
			int end2 = element.toString().indexOf("<a");

			if (end1 == -1) {
				if (end2 == -1)
					anchorman = element.text().replace("\n", "").trim();
				else
					anchorman = element.toString().substring(start, end2)
							.replace("\n", "").trim();
			} else {
				anchorman = element.toString().substring(start, end1)
						.replace("\n", "").trim();
			}

			String info = infos.get(i).text().trim();
			String totalPlayCount = totalPlayCounts.get(i).text().trim();
			Album album = new Album(title, anchorman, info, totalPlayCount);
			albums.add(album);
		}
	}

	/*
	 * 转换播放数量表达方式
	 */
	private static String transPlayCount(String pc) {
		int index;
		if ((index = pc.indexOf("万")) != -1) {
			String count = pc.substring(0, index);
			return String.valueOf((Double.parseDouble(count) * 10000));
		}

		return pc;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "resource" })
	public static void main(String args[]) {
		SearchAlbum searchAlbum = new SearchAlbum();
		Scanner sc = new Scanner(System.in);
		while(true)
		{
			System.out.print("请输入搜索的关键词(输入\"exit\"退出程序):");
			searchWord = sc.nextLine();
			
			if(searchWord.equals("exit"))
				break;
			
			long startTime = System.currentTimeMillis();

			// 开始爬虫
			System.out.println("开始搜索关键字:" + searchWord + ",请等待...");
			//搜索所有分页
			while(totalPage >= currentPage)
			{
				// 获得请求
				String param = request.replace("#searchWord#",searchAlbum.getEncodeWord(searchWord)).replace("#currentPage#",String.valueOf(currentPage));
				//获取url+param请求的网页数据
				Document doc = searchAlbum.getUrlContent(host, param);
				//网页数据分析
				searchAlbum.getHref(doc);
				++currentPage;
			}
			long endTime = System.currentTimeMillis();
			float spendTime = (endTime - startTime) / 1000F;
			// 排序
			Collections.sort(albums, new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					if (o1 instanceof Album && o2 instanceof Album) {
						Album a1 = (Album) o1;
						Album a2 = (Album) o2;
						String c1 = a1.getTotalPlayCount();
						String c2 = a2.getTotalPlayCount();
						return new Double(transPlayCount(c2)).compareTo(new Double(
								transPlayCount(c1)));
					}
					return 0;
				}
			});
			System.out.println("爬取网页数量:" + searchPageNum + ",消耗时间:" + spendTime
					+ "秒,搜索结果数量:" + albums.size() + "条,分页数量:" + totalPage);
			for (Album a : albums)
				System.out.println(a);
			
			currentPage = 1;
			totalPage = 1;
			searchPageNum = 0;
			albums.clear();
		}
			
	}
		
}
