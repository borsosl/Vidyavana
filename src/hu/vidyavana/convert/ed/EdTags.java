package hu.vidyavana.convert.ed;

import static hu.vidyavana.convert.api.ParagraphClass.*;
import hu.vidyavana.convert.api.ParagraphClass;
import java.util.TreeMap;

public enum EdTags
{
	// special markers for the program
	info(""),
	unhandled(""),
	continuing(""),

	// normal ed tags
	ac_bhakti("ac bhakti"),
	asterisk("asterisk"),
	benbo("benbo"),
	bengali("bengali"),
	benro("benro"),
	biglet("biglet"),
	biglet1("biglet1"),
	book_title("book title"),
	center("center", Kozepen),
	center_line("center line"),
	center_table("center table"),
	ch_line("ch line"),
	ch_title_f("ch title f"),
	ch_title_f2("ch title f2"),
	ch_title_fb("ch title fb"),
	ch_title_fm("ch title fm"),
	ch_title_i("ch title i"),
	ch_verse("ch verse"),
	chapter("chapter", Fejezetszam),
	chapter_p1("chapter +1"),
	chapter_p2("chapter +2"),
	chapter_m1("chapter -1"),
	chapter_head("chapter head"),
	chapter_more("chapter more"),
	chapter_nospa("chapter nospa"),
	chapter_spec("chapter spec"),
	chapter_title("chapter title", Fejezetcim),
	chaptno("chaptno"),
	date_no_space("date no space"),
	date_space("date space"),
	datesmal("datesmal"),
	dc_body_l("dc body l"),
	dc_body_r("dc body r"),
	dc_left_top("dc left top"),
	dc_right_top("dc right top"),
	dev1("dev1"),
	dev1b("dev1b"),
	dev1e("dev1e"),
	dev2("dev2"),
	dev2b("dev2b"),
	dev2bk("dev2bk"),
	dev2bl("dev2bl"),
	dev2e("dev2e"),
	dev2el("dev2el"),
	dev3("dev3"),
	dev3b("dev3b"),
	dev3e("dev3e"),
	devanagari("devanagari"),
	ds_title("ds title"),
	end("end"),
	eop("eop"),
	fl_body("fl body"),
	fl_body_1("fl body 1"),
	foot_line("foot line"),
	footnote("footnote"),
	gl_first("gl first"),
	gl_purp("gl purp"),
	gloss_head("gloss head"),
	glossary("glossary"),
	header("header"),
	history("history"),
	history_no("history no"),
	index_letter("index letter"),
	index_level_0("index level 0", Index),
	index_level_1("index level 1"),
	index_level_2("index level 2"),
	index_level_3("index level 3"),
	intro("intro"),
	introduction("introduction"),
	italic_para("italic para"),
	keep_p_n("keep p n"),
	keep_p_p("keep p p"),
	keep_pp_n("keep pp n"),
	keep_pp_p("keep pp p"),
	konyvcim("konyvcim", Konyvcim),
	lila("lila"),
	line("line"),
	long_verse("long verse"),
	middel_long("middel long"),
	mpurp("mpurp"),
	nnpurp("nnpurp"),
	notes("notes"),
	npurp("npurp"),
	number("number"),
	om_tat_sat("om tat sat"),
	p_tab("p tab"),
	para_2_orp("para 2 orp"),
	para_no_break("para no break"),
	poem("poem"),
	poem_nr("poem nr"),
	pp_tab("pp tab"),
	pr_pic("pr_pic"),
	prose("prose", Proza),
	prose_in_purp("prose in purp"),
	purp_1orp("purp 1orp"),
	purp_no_break("purp no break"),
	purp_para("purp para", TorzsKoveto),
	purp_small("purp small"),
	purp_space("purp space", Kozepen, "@@@"),
	purport("purport", TorzsKezdet),
	push_1_point("push 1 point"),
	push_2("push 2"),
	push_3_point("push 3 point"),
	push_6("push 6"),
	push_6_point("push 6 point"),
	push2("push2"),
	push3("push3"),
	push5("push5"),
	push8("push8"),
	q_ind_s_pa("q ind s pa"),
	q_ind_s_pu("q ind s pu"),
	quote("quote", MegjegyzesKezdet),
	quote_center("quote center"),
	quote_in_purp("quote in purp"),
	quote_ind_pa("quote ind pa"),
	quote_ind_pu("quote ind pu"),
	quote_keep("quote keep"),
	quote_para("quote para", MegjegyzesKoveto),
	quote_purp("quote purp"),
	quote_purp_sp("quote purp sp"),
	quote_sign("quote sign", MegjegyzesJobbra),
	right_align("right align", Jobbra),
	sans_7p("sans 7p"),
	sans_in_id("sans in id"),
	sans_no_b("sans no b"),
	sans_uvaca("sans uvaca", Uvaca),
	sans_uvaca_id("sans uvaca id"),
	sanskrit("sanskrit", Vers),
	signature("signature"),
	sloka("sloka"),
	small_foot("small foot"),
	special("special"),
	spurp("spurp"),
	sspurp("sspurp"),
	subsub("subsub"),
	subtit3("subtit3"),
	subtit4("subtit4"),
	subtit5("subtit5"),
	subtitle("subtitle", Alcim),
	text("text", Versszam),
	text_k_p("text k p"),
	textno("textno"),
	trans_keep("trans keep"),
	trans_nob("trans nob"),
	translation("translation", Forditas),
	uvaca_in_purp("uvaca in purp"),
	uvaca_on_top("uvaca on top"),
	uvaca_verse("uvaca verse"),
	ver_in_purp1("ver in purp1"),
	verse_i_p_7p("verse i p 7p"),
	verse_in_purp("verse in purp", TorzsVers),
	verse_ref("verse ref", Hivatkozas),
	verse_short("verse short"),
	verse_uvaca("verse uvaca", TorzsUvaca),
	very_long("very long"),
	w_by_w_nob("w by w nob"),
	wa_body("wa body"),
	wa_number("wa number"),
	wa_side("wa side"),
	word_by_word("word by word", Szavak),
	word_keep("word keep"),
	word_n_b("word n b"),
	xi_head_text("xi head text"),
	xi0("xi0"),
	xi0_first("xi0 first"),
	xi0_last("xi0 last"),
	xi1("xi1"),
	xi1_last("xi1 last"),
	xi2("xi2"),
	xi2_last("xi2 last"),
	xs_head_text("xs head text");

	
	private static EdTags[][] aliases = {
		{konyvcim},
		{chapter,
			chapter_p1, chapter_p2, chapter_m1},
		{chapter_title,
			chapter_spec, chapter_more, chapter_nospa, ch_title_f, ch_title_f2, ch_title_fb, ch_title_fm,
			ch_title_i, ds_title, intro, history, history_no},
		{text},
		{sans_uvaca,
			sans_uvaca_id, sans_in_id},
		{sanskrit,
			bengali, long_verse, sans_7p, sans_no_b},
		{prose},
		{word_by_word,
			w_by_w_nob, word_keep, word_n_b},
		{translation,
			trans_keep, trans_nob},
		{purport,
			biglet, biglet1, date_no_space, date_space, datesmal, dc_left_top, end, fl_body_1, footnote,
			gl_first, gl_purp, introduction, keep_p_n, keep_p_p, notes, p_tab, prose_in_purp, purp_1orp,
			purp_no_break, purp_small, small_foot, wa_side, xi_head_text, xs_head_text},
		{purp_para,
			dc_body_l, dc_body_r, dc_right_top, fl_body, glossary, italic_para, keep_pp_n, keep_pp_p,
			pp_tab, para_2_orp, wa_body},
		{purp_space,
			asterisk},
		{verse_uvaca,
			uvaca_verse},
		{verse_in_purp,
			ch_verse, middel_long, ver_in_purp1, verse_i_p_7p, 
			uvaca_in_purp, uvaca_on_top, verse_short, very_long},
		{verse_ref},
		{center,
			center_line, center_table, gloss_head, header, number, poem_nr, quote_center},
		{right_align,
			om_tat_sat, signature, ac_bhakti},
		{subtitle,
			subsub, subtit3, subtit4, subtit5, index_letter},
		{quote,
			sloka, poem, q_ind_s_pu, quote_ind_pu, quote_in_purp, quote_keep, quote_purp, quote_purp_sp},
		{quote_para,
			q_ind_s_pa, quote_ind_pa},
		{quote_sign},
		{index_level_0,
			index_level_1, index_level_2, index_level_3, xi0, xi0_first, xi0_last, xi1, xi1_last, xi2, xi2_last},
		{continuing,
			mpurp, npurp, nnpurp, para_no_break, spurp, sspurp},
		{info,
			book_title, lila, chaptno, chapter_head, textno},
		{unhandled,
			benbo, benro, ch_line, /* dev...*/ eop, foot_line, line,
			pr_pic, /* push...*/ special, wa_number}};

	
	public static TreeMap<String, EdTags> map = new TreeMap<>();
	static
	{
		for(EdTags edt : EdTags.values())
		{
			map.put(edt.tag, edt);
			if(edt.tag.startsWith("dev") || edt.tag.startsWith("push"))
				edt.alias = unhandled;
		}
		for(EdTags[] ar : aliases)
		{
			EdTags root = ar[0];
			for(int i=1; i<ar.length; ++i)
				ar[i].alias = root;
		}
	}

	
	public final String tag;
	public final ParagraphClass cls;
	public final String style;
	public EdTags alias;

	
	EdTags(String tag)
	{
		this.tag = tag;
		cls = null;
		style = null;
	}

	
	EdTags(String tag, ParagraphClass cls)
	{
		this.tag = tag;
		this.cls = cls;
		style = null;
	}

	
	EdTags(String tag, ParagraphClass cls, String style)
	{
		this.tag = tag;
		this.cls = cls;
		this.style = style;
	}

	
	public static EdTags find(String tagStr)
	{
		return map.get(tagStr);
	}
}
