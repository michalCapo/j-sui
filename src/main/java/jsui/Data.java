package jsui;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Java port of key data helpers from t-sui/ui.data.ts.
 *
 * Provides NormalizeForSearch, table/filtering models, and a Collate UI
 * component that mirrors the TypeScript behaviour using only JDK classes.
 */
public final class Data {
    private Data() {
    }

    // ---------------------------------------------------------------------
    // NormalizeForSearch

    public static String NormalizeForSearch(String input) {
        String s = (input == null ? "" : input).toLowerCase(Locale.ROOT);
        String[][] repl = new String[][] {
                { "á", "a" }, { "ä", "a" }, { "à", "a" }, { "â", "a" }, { "ã", "a" }, { "å", "a" }, { "æ", "ae" },
                { "č", "c" }, { "ć", "c" }, { "ç", "c" }, { "ď", "d" }, { "đ", "d" }, { "é", "e" }, { "ë", "e" },
                { "è", "e" }, { "ê", "e" }, { "ě", "e" }, { "í", "i" }, { "ï", "i" }, { "ì", "i" }, { "î", "i" },
                { "ľ", "l" }, { "ĺ", "l" }, { "ł", "l" }, { "ň", "n" }, { "ń", "n" }, { "ñ", "n" }, { "ó", "o" },
                { "ö", "o" }, { "ò", "o" }, { "ô", "o" }, { "õ", "o" }, { "ø", "o" }, { "œ", "oe" }, { "ř", "r" },
                { "ŕ", "r" }, { "š", "s" }, { "ś", "s" }, { "ş", "s" }, { "ș", "s" }, { "ť", "t" }, { "ț", "t" },
                { "ú", "u" }, { "ü", "u" }, { "ù", "u" }, { "û", "u" }, { "ů", "u" }, { "ý", "y" }, { "ÿ", "y" },
                { "ž", "z" }, { "ź", "z" }, { "ż", "z" }
        };
        for (String[] kv : repl) {
            s = s.replace(kv[0], kv[1]);
        }
        return s;
    }

    // ---------------------------------------------------------------------
    // Constants and models

    public static final int BOOL = 0;
    public static final int NOT_ZERO_DATE = 1;
    public static final int ZERO_DATE = 2;
    public static final int DATES = 3;
    public static final int SELECT = 4;

    public static final List<Ui.AOption> BOOL_ZERO_OPTIONS;
    static {
        List<Ui.AOption> opts = new ArrayList<>();
        opts.add(new Ui.AOption("", "All"));
        opts.add(new Ui.AOption("yes", "On"));
        opts.add(new Ui.AOption("no", "Off"));
        BOOL_ZERO_OPTIONS = Collections.unmodifiableList(opts);
    }

    public static final class TFieldDates {
        public Date From;
        public Date To;
    }

    public static final class TField {
        public String DB;
        public String Field;
        public String Text;

        public String Value;
        public int As;
        public String Condition;
        public List<Ui.AOption> Options = new ArrayList<>();

        public boolean Bool;
        public TFieldDates Dates;
    }

    public static final class TQuery {
        public int Limit;
        public int Offset;
        public String Order = "";
        public String Search = "";
        public List<TField> Filter = new ArrayList<>();
    }

    public static final class TCollateResult<T> {
        public int Total;
        public int Filtered;
        public List<T> Data = new ArrayList<>();
        public TQuery Query;
    }

    public static final class LoadResult<T> {
        public int total;
        public int filtered;
        public List<T> data = new ArrayList<>();
    }

    public interface Loader<T> {
        LoadResult<T> load(TQuery query) throws Exception;
    }

    public interface CollateModel<T> {
        void setSort(List<TField> fields);

        void setFilter(List<TField> fields);

        void setSearch(List<TField> fields);

        void setExcel(List<TField> fields);

        void Row(RenderRow<T> fn);

        void Export(Export<T> fn);

        String Render(Context ctx);
    }

    public interface RenderRow<T> {
        String render(T item, int index);
    }

    public interface Export<T> {
        void export(List<T> items) throws Exception;
    }

    // ---------------------------------------------------------------------
    // Collate (Java port)

    public static <T> CollateModel<T> Collate(TQuery init, Loader<T> loader) {
        final State<T> state = new State<>();
        state.Init = makeQuery(init);
        state.Target = Ui.Target();
        state.TargetFilter = Ui.Target();
        state.Loader = loader;

        state.ActionSearch = ctx -> handleSearch(state, ctx);
        state.ActionSort = ctx -> handleSort(state, ctx);
        state.ActionReset = ctx -> handleReset(state, ctx);
        state.ActionResize = ctx -> handleResize(state, ctx);
        state.ActionExcel = ctx -> handleExcel(state, ctx);

        return new CollateModel<T>() {
            @Override
            public void setSort(List<TField> fields) {
                state.SortFields = copyFields(fields);
            }

            @Override
            public void setFilter(List<TField> fields) {
                state.FilterFields = copyFields(fields);
            }

            @Override
            public void setSearch(List<TField> fields) {
                state.SearchFields = copyFields(fields);
            }

            @Override
            public void setExcel(List<TField> fields) {
                state.ExcelFields = copyFields(fields);
            }

            @Override
            public void Row(RenderRow<T> fn) {
                state.OnRow = fn;
            }

            @Override
            public void Export(Export<T> fn) {
                state.OnExcel = fn;
            }

            @Override
            public String Render(Context ctx) {
                TQuery query = makeQuery(state.Init);
                TCollateResult<T> result = triggerLoad(state, query);
                return renderUI(ctx, state, query, result, false);
            }
        };
    }

    private static <T> String handleSearch(State<T> state, Context ctx) throws Exception {
        TQuery query = makeQuery(state.Init);
        applyRequest(ctx, query);
        normalizeQuery(query, state.Init);
        TCollateResult<T> result = triggerLoad(state, query);
        return renderUI(ctx, state, query, result, false);
    }

    private static <T> String handleSort(State<T> state, Context ctx) throws Exception {
        TQuery query = makeQuery(state.Init);
        applyRequest(ctx, query);
        normalizeQuery(query, state.Init);
        TCollateResult<T> result = triggerLoad(state, query);
        return renderUI(ctx, state, query, result, false);
    }

    private static <T> String handleReset(State<T> state, Context ctx) throws Exception {
        TQuery query = makeQuery(state.Init);
        normalizeQuery(query, state.Init);
        TCollateResult<T> result = triggerLoad(state, query);
        return renderUI(ctx, state, query, result, false);
    }

    private static <T> String handleResize(State<T> state, Context ctx) throws Exception {
        TQuery query = makeQuery(state.Init);
        applyRequest(ctx, query);
        normalizeQuery(query, state.Init);
        if (query.Limit <= 0) {
            query.Limit = state.Init != null && state.Init.Limit > 0 ? state.Init.Limit : 10;
        }
        query.Limit = query.Limit * 2;
        TCollateResult<T> result = triggerLoad(state, query);
        return renderUI(ctx, state, query, result, false);
    }

    private static <T> String handleExcel(State<T> state, Context ctx) {
        try {
            TQuery query = makeQuery(state.Init);
            applyRequest(ctx, query);
            // export all rows up to a reasonable cap
            if (query.Limit <= 0) query.Limit = 1000000;
            query.Offset = 0;
            normalizeQuery(query, state.Init);
            TCollateResult<T> result = triggerLoad(state, query);
            if (result == null || result.Data == null) {
                ctx.Info("No data to export.");
                return "";
            }

            // Build CSV using ExcelFields if provided, else use Filter/Sort fields, else reflect all public fields
            List<TField> headers = state.ExcelFields != null && !state.ExcelFields.isEmpty()
                    ? state.ExcelFields
                    : (state.SortFields != null && !state.SortFields.isEmpty() ? state.SortFields : state.FilterFields);

            StringBuilder csv = new StringBuilder();
            List<String> headerNames = new ArrayList<>();
            if (headers != null && !headers.isEmpty()) {
                for (TField h : headers) {
                    String name = (h != null && h.Text != null && !h.Text.isEmpty()) ? h.Text : (h != null ? (h.Field != null ? h.Field : h.DB) : "");
                    headerNames.add(name != null ? name : "");
                }
            } else {
                // Fallback: inspect first row fields via reflection
                if (!result.Data.isEmpty()) {
                    for (java.lang.reflect.Field f : result.Data.get(0).getClass().getFields()) {
                        headerNames.add(f.getName());
                    }
                }
            }
            // write header
            csv.append(String.join(",", escapeCsv(headerNames))).append("\n");
            // write rows
            for (T item : result.Data) {
                List<String> row = new ArrayList<>();
                if (headers != null && !headers.isEmpty()) {
                    for (TField h : headers) {
                        String field = h != null && h.Field != null && !h.Field.isEmpty() ? h.Field : (h != null ? h.DB : null);
                        row.add(stringField(item, field));
                    }
                } else {
                    for (java.lang.reflect.Field f : item.getClass().getFields()) {
                        row.add(stringValue(get(item, f)));
                    }
                }
                csv.append(String.join(",", escapeCsv(row))).append("\n");
            }

            String dataUrl = "data:text/csv;charset=utf-8," + java.net.URLEncoder.encode(csv.toString(), java.nio.charset.StandardCharsets.UTF_8);
            // trigger download without changing the page
            return Ui.Script("(function(){var a=document.createElement('a');a.href=\"" + dataUrl + "\";a.download='export.csv';document.body.appendChild(a);a.click();setTimeout(function(){try{document.body.removeChild(a);}catch(_){}} ,0);} )();");
        } catch (Exception ex) {
            ctxError(state, ex);
            ctx.Error("Export failed: " + ex.getMessage());
            return "";
        }
    }

    private static <T> Object get(T item, java.lang.reflect.Field f) {
        try {
            return f.get(item);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static String stringField(Object item, String name) {
        if (item == null || name == null || name.isEmpty()) return "";
        try {
            java.lang.reflect.Field f = item.getClass().getField(name);
            return stringValue(get(item, f));
        } catch (NoSuchFieldException e) {
            try {
                java.lang.reflect.Field f = item.getClass().getDeclaredField(name);
                f.setAccessible(true);
                return stringValue(get(item, f));
            } catch (NoSuchFieldException ex) {
                return "";
            }
        }
    }

    private static List<String> escapeCsv(List<String> values) {
        List<String> out = new ArrayList<>();
        for (String v : values) out.add(escapeCsv(v));
        return out;
    }

    private static String escapeCsv(String v) {
        if (v == null) return "";
        boolean needsQuote = v.contains(",") || v.contains("\n") || v.contains("\"");
        String s = v.replace("\"", "\"\"");
        return needsQuote ? ("\"" + s + "\"") : s;
    }

    private static String stringValue(Object v) {
        if (v == null) return "";
        if (v instanceof java.util.Date) return formatDate((java.util.Date) v);
        return String.valueOf(v);
    }

    private static <T> TCollateResult<T> triggerLoad(State<T> state, TQuery query) {
        TCollateResult<T> out = new TCollateResult<>();
        out.Query = copyQuery(query);
        if (state.Loader == null) {
            return out;
        }
        try {
            LoadResult<T> load = state.Loader.load(copyQuery(query));
            if (load != null) {
                out.Total = load.total;
                out.Filtered = load.filtered;
                out.Data = load.data != null ? load.data : new ArrayList<>();
            }
        } catch (Exception ex) {
            ctxError(state, ex);
            out.Total = 0;
            out.Filtered = 0;
            out.Data = new ArrayList<>();
        }
        return out;
    }

    private static <T> void ctxError(State<T> state, Exception ex) {
        // placeholder hook for future logging; keep silent for now
        if (state != null && state.Debug && ex != null) {
            ex.printStackTrace();
        }
    }

    private static <T> String renderUI(Context ctx, State<T> state, TQuery query, TCollateResult<T> result,
            boolean loading) {
        String header = renderHeader(ctx, state, query, loading);
        if (loading || result == null) {
            String skeletonRows = Ui.Skeleton.List(Ui.Target(), 6);
            String skeletonPager = Ui.div("flex items-center justify-center").render(
                    Ui.div("mx-4 font-bold text-lg").render("\u00A0"),
                    Ui.div("flex gap-px flex-1 justify-end").render(
                            Ui.div("bg-gray-200 h-9 w-10 rounded-l border").render(),
                            Ui.div("bg-gray-200 h-9 w-36 rounded-r border").render()));
            return Ui.div("flex flex-col gap-2 mt-2", Ui.targetAttr(state.Target)).render(header, skeletonRows,
                    skeletonPager);
        }

        String rows = renderRows(result.Data, state.OnRow);
        String pager = renderPager(ctx, state, result);
        return Ui.div("flex flex-col gap-2 mt-2", Ui.targetAttr(state.Target)).render(header, rows, pager);
    }

    private static <T> String renderHeader(Context ctx, State<T> state, TQuery query, boolean loading) {
        String sorting = renderSorting(ctx, state, query);
        String searching = renderSearching(ctx, state, query);
        String filtering = renderFiltering(ctx, state, query);
        String wrapperCls = loading ? "flex flex-col pointer-events-none" : "flex flex-col";
        return Ui.div(wrapperCls).render(
                Ui.div("flex gap-x-2").render(
                        sorting,
                        Ui.Flex1,
                        searching),
                Ui.div("flex justify-end").render(filtering));
    }

    private static <T> String renderSorting(Context ctx, State<T> state, TQuery query) {
        List<TField> sortFields = state.SortFields;
        if (sortFields == null || sortFields.isEmpty()) {
            return "";
        }
        List<String> buttons = new ArrayList<>();
        for (TField sort : sortFields) {
            if (sort == null) {
                continue;
            }
            String db = sort.DB != null && !sort.DB.isEmpty() ? sort.DB : sort.Field;
            if (db == null) {
                continue;
            }
            String field = db.toLowerCase(Locale.ROOT);
            String order = query.Order != null ? query.Order.toLowerCase(Locale.ROOT) : "";
            String direction = "";
            String color = Ui.GrayOutline;
            if (order.startsWith(field + " ") || order.equals(field)) {
                direction = order.contains("asc") ? "asc" : "desc";
                color = Ui.Purple;
            }
            String reverse = "desc";
            if ("desc".equals(direction)) {
                reverse = "asc";
            }
            List<String> children = new ArrayList<>();
            children.add(hiddenInput("Order", db + " " + reverse));
            children.add(hiddenInput("Search", query.Search));
            children.add(hiddenInput("Limit", Integer.toString(query.Limit)));
            children.add(hiddenInput("Offset", "0"));
            children.addAll(hiddenFilterInputs(query));
            String button = new Ui.Button()
                    .Submit()
                    .Class("bg-white rounded")
                    .Color(color)
                    .Render(
                            Ui.div("flex gap-2 items-center").render(
                                    directionIcon(direction),
                                    sort.Text != null ? sort.Text : db));
            children.add(button);
            String form = Ui.form("inline-flex", ctx.Submit(state.ActionSort).Replace(Ui.targetAttr(state.Target)))
                    .render(children.toArray(new String[0]));
            buttons.add(form);
        }
        return Ui.div("flex gap-1").render(buttons.toArray(new String[0]));
    }

    private static String directionIcon(String direction) {
        if ("asc".equals(direction)) {
            return Ui.Icon("fa fa-fw fa-sort-amount-asc");
        }
        if ("desc".equals(direction)) {
            return Ui.Icon("fa fa-fw fa-sort-amount-desc");
        }
        return Ui.Icon("fa fa-fw fa-sort");
    }

    private static <T> String renderSearching(Context ctx, State<T> state, TQuery query) {
        List<String> children = new ArrayList<>();
        String clearJs = "(function(b){try{var f=b.closest('form');if(!f)return;var i=f.querySelector(\"[name='Search']\");if(i){i.value='';}f.submit();}catch(_){}})(this)";
        String form = Ui.form("flex", ctx.Submit(state.ActionSearch).Replace(Ui.targetAttr(state.Target))).render(
            Ui.div("relative flex-1 w-72").render(
                Ui.div("absolute left-3 top-1/2 transform -translate-y-1/2").render(
                    new Ui.Button()
                        .Submit()
                        .Class("rounded-full bg-white hover:bg-gray-100 h-8 w-8 border border-gray-300 flex items-center justify-center")
                        .Render(Ui.Icon("fa fa-fw fa-search"))
                ),
                Ui.IText("Search", query)
                    .Class("p-1 w-full")
                    .ClassInput("cursor-pointer bg-white border-gray-300 hover:border-blue-500 block w-full py-3 pl-12 pr-12")
                    .Placeholder("Search")
                    .Render(""),
                (query.Search != null && !query.Search.isEmpty())
                    ? Ui.div("absolute right-3 top-1/2 transform -translate-y-1/2").render(
                        new Ui.Button()
                            .Class("rounded-full bg-white hover:bg-gray-100 h-8 w-8 border border-gray-300 flex items-center justify-center")
                            .Click(clearJs)
                            .Render(Ui.Icon("fa fa-fw fa-times"))
                    )
                    : ""
            )
        );
        children.add(form);

        if (state.ExcelFields != null && !state.ExcelFields.isEmpty()) {
            String excel = new Ui.Button()
                .Color(Ui.Blue)
                .Class("rounded-lg shadow px-4 h-12 bg-white text-blue-700 flex items-center gap-2")
                .Click(ctx.Call(state.ActionExcel).None())
                .Render(Ui.IconLeft("fa fa-download", "XLS"));
            children.add(excel);
        }

        if (state.FilterFields != null && !state.FilterFields.isEmpty()) {
            String toggle = new Ui.Button()
                .Submit()
                .Class("rounded-r-lg shadow bg-white h-12 px-4 flex items-center gap-2")
                .Color(Ui.Blue)
                .Click("var el=document.getElementById('" + state.TargetFilter.id + "'); if(el){el.classList.toggle('hidden');}")
                .Render(Ui.IconLeft("fa fa-fw fa-chevron-down", "Filter"));
            children.add(toggle);
        }

        return Ui.div("flex gap-px bg-blue-800 rounded-lg p-1 items-center").render(children.toArray(new String[0]));
    }

    private static <T> String renderFiltering(Context ctx, State<T> state, TQuery query) {
        List<TField> filterFields = state.FilterFields;
        if (filterFields == null || filterFields.isEmpty()) {
            return "";
        }
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < filterFields.size(); i++) {
            TField def = filterFields.get(i);
            if (def == null) {
                continue;
            }
            TField target = ensureFilter(query, i, def);
            String position = "Filter." + i;
            List<String> parts = new ArrayList<>();
            if (def.As == ZERO_DATE || def.As == NOT_ZERO_DATE) {
                parts.add(Ui.Hidden(position + ".Field", "string", target.DB));
                parts.add(Ui.Hidden(position + ".As", "number", Integer.toString(def.As)));
                parts.add(hiddenCheckbox(position + ".Bool", target.Bool));
                parts.add(Ui.ICheckbox(position + ".Bool", query).Render(def.Text));
            } else if (def.As == DATES) {
                parts.add(Ui.Hidden(position + ".Field", "string", target.DB));
                parts.add(Ui.Hidden(position + ".As", "number", Integer.toString(def.As)));
                parts.add(Ui.IDate(position + ".Dates.From", query).Render("From"));
                parts.add(Ui.IDate(position + ".Dates.To", query).Render("To"));
            } else if (def.As == SELECT) {
                parts.add(Ui.Hidden(position + ".Field", "string", target.DB));
                parts.add(Ui.Hidden(position + ".As", "number", Integer.toString(def.As)));
                Ui.ISelect select = Ui.ISelect(position + ".Value", query)
                        .Options(target.Options != null && !target.Options.isEmpty() ? target.Options : def.Options)
                        .Value(target.Value != null ? target.Value : "");
                parts.add(select.Render(def.Text));
            } else if (def.As == BOOL) {
                parts.add(Ui.Hidden(position + ".Field", "string", target.DB));
                parts.add(Ui.Hidden(position + ".As", "number", Integer.toString(def.As)));
                parts.add(Ui.Hidden(position + ".Condition", "string",
                        target.Condition != null ? target.Condition : def.Condition));
                parts.add(hiddenCheckbox(position + ".Bool", target.Bool));
                parts.add(Ui.ICheckbox(position + ".Bool", query).Render(def.Text));
            }
            rows.add(Ui.div("col-span-2 flex flex-col gap-2").render(parts.toArray(new String[0])));
        }

        List<String> children = new ArrayList<>();
        children.add(Ui.Hidden("Search", "string", query.Search));
        children.add(Ui.Hidden("Order", "string", query.Order));
        children.add(Ui.Hidden("Limit", "number", Integer.toString(query.Limit)));
        children.add(Ui.Hidden("Offset", "number", "0"));
        children.addAll(rows);

        String buttons = Ui.div("flex justify-end gap-2 mt-6 pt-3 border-t border-gray-200").render(
                new Ui.Button()
                        .Submit()
                        .Class("rounded-full h-10 px-4 bg-white")
                        .Color(Ui.GrayOutline)
                        .Click(resetFiltersJs(state.TargetFilter.id))
                        .Render(Ui.IconLeft("fa fa-fw fa-rotate-left", "Reset")),
                new Ui.Button()
                        .Submit()
                        .Class("rounded-full h-10 px-4 shadow")
                        .Color(Ui.Blue)
                        .Render(Ui.IconLeft("fa fa-fw fa-check", "Apply")));
        children.add(buttons);

        String form = Ui.form("flex flex-col p-4", ctx.Submit(state.ActionSearch).Replace(Ui.targetAttr(state.Target)))
                .render(children.toArray(new String[0]));
        return Ui.div("col-span-2 relative h-0 hidden z-30", Ui.targetAttr(state.TargetFilter)).render(
                Ui.div("absolute top-2 right-0 w-96 bg-white rounded-xl shadow-xl ring-1 ring-black/10 border border-gray-200")
                        .render(form));
    }

    private static String resetFiltersJs(String targetId) {
        return "(function(btn){try{var form=btn.closest('form');if(!form)return;var fields=form.querySelectorAll(\"[name^='Filter.']\");for(var i=0;i<fields.length;i++){var el=fields[i];if(!el)continue;var type=(el.getAttribute('type')||'').toLowerCase();if(type==='checkbox'){el.checked=false;}else{el.value='';}}}catch(_){}})(this)";
    }

    private static String hiddenCheckbox(String name, boolean value) {
        return Ui.input("", Ui.Attr.of().type("hidden").name(name).value(Boolean.toString(value)));
    }

    private static List<String> hiddenFilterInputs(TQuery query) {
        List<String> hidden = new ArrayList<>();
        List<TField> filters = query.Filter;
        if (filters == null) {
            return hidden;
        }
        for (int i = 0; i < filters.size(); i++) {
            TField f = filters.get(i);
            if (f == null) {
                continue;
            }
            String prefix = "Filter." + i;
            hidden.add(hiddenInput(prefix + ".Field", valueOr(f.DB, f.Field)));
            hidden.add(hiddenInput(prefix + ".As", Integer.toString(f.As)));
            if (f.Condition != null) {
                hidden.add(hiddenInput(prefix + ".Condition", f.Condition));
            }
            if (f.Value != null) {
                hidden.add(hiddenInput(prefix + ".Value", f.Value));
            }
            hidden.add(hiddenInput(prefix + ".Bool", Boolean.toString(f.Bool)));
            if (f.Dates != null) {
                hidden.add(hiddenInput(prefix + ".Dates.From", formatDate(f.Dates.From)));
                hidden.add(hiddenInput(prefix + ".Dates.To", formatDate(f.Dates.To)));
            }
        }
        return hidden;
    }

    private static String hiddenInput(String name, String value) {
        return Ui.input("", Ui.Attr.of().type("hidden").name(name).value(value != null ? value : ""));
    }

    private static String valueOr(String primary, String fallback) {
        if (primary != null && !primary.isEmpty()) {
            return primary;
        }
        return fallback != null ? fallback : "";
    }

    private static <T> String renderPager(Context ctx, State<T> state, TCollateResult<T> result) {
        if (result == null) {
            return "";
        }
        if (result.Filtered == 0) {
            return emptyState(result);
        }
        int size = result.Data != null ? result.Data.size() : 0;
        String count = "Showing " + size + " / " + result.Filtered;
        if (result.Filtered != result.Total) {
            count += " of " + result.Total + " in total";
        }
        List<String> resetChildren = new ArrayList<>();
        resetChildren.add(hiddenInput("Search", result.Query != null ? result.Query.Search : ""));
        resetChildren.add(hiddenInput("Order", result.Query != null ? result.Query.Order : ""));
        resetChildren.add(hiddenInput("Limit", Integer.toString(result.Query != null ? result.Query.Limit : 10)));
        resetChildren.add(hiddenInput("Offset", "0"));
        resetChildren.addAll(hiddenFilterInputs(result.Query != null ? result.Query : new TQuery()));
        resetChildren.add(new Ui.Button()
                .Submit()
                .Class("bg-white rounded-l h-10 px-4")
                .Color(Ui.PurpleOutline)
                .Disabled(size == 0 || size <= Math.max(1, result.Query != null ? result.Query.Limit : 10))
                .Render(Ui.Icon("fa fa-fw fa-undo")));
        String resetForm = Ui.form("inline-flex", ctx.Submit(state.ActionReset).Replace(Ui.targetAttr(state.Target)))
                .render(resetChildren.toArray(new String[0]));

        List<String> moreChildren = new ArrayList<>();
        moreChildren.add(hiddenInput("Search", result.Query != null ? result.Query.Search : ""));
        moreChildren.add(hiddenInput("Order", result.Query != null ? result.Query.Order : ""));
        moreChildren.add(hiddenInput("Limit", Integer.toString(result.Query != null ? result.Query.Limit : 10)));
        moreChildren.add(hiddenInput("Offset", Integer.toString(result.Query != null ? result.Query.Offset : 0)));
        moreChildren.addAll(hiddenFilterInputs(result.Query != null ? result.Query : new TQuery()));
        moreChildren.add(new Ui.Button()
                .Submit()
                .Class("rounded-r h-10 px-4")
                .Color(Ui.Purple)
                .Disabled(size >= result.Filtered)
                .Render(Ui.div("flex gap-2 items-center").render(
                        Ui.Icon("fa fa-arrow-down"),
                        "Load more items")));
        String moreForm = Ui.form("inline-flex", ctx.Submit(state.ActionResize).Replace(Ui.targetAttr(state.Target)))
                .render(moreChildren.toArray(new String[0]));

        return Ui.div("flex items-center justify-center").render(
                Ui.div("mx-4 font-bold text-lg").render(count),
                Ui.div("flex gap-px flex-1 justify-end").render(resetForm, moreForm));
    }

    private static <T> String emptyState(TCollateResult<T> result) {
        if (result.Total == 0) {
            return Ui.div("mt-2 py-24 rounded text-xl flex justify-center items-center bg-white rounded-lg").render(
                    Ui.div("").render(
                            Ui.div("text-black text-2xl p-4 mb-2 font-bold flex justify-center items-center")
                                    .render("No records found")));
        }
        return Ui.div("mt-2 py-24 rounded text-xl flex justify-center items-center bg-white rounded-lg").render(
                Ui.div("flex gap-x-px items-center justify-center text-2xl").render(
                        Ui.Icon("fa fa-fw fa-exclamation-triangle text-yellow-500"),
                        Ui.div("text-black p-4 mb-2 font-bold flex justify-center items-center")
                                .render("No records found for the selected filter")));
    }

    private static <T> String renderRows(List<T> data, RenderRow<T> onRow) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        if (onRow == null) {
            return Ui.div("").render("Missing row renderer");
        }
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            rows.add(onRow.render(data.get(i), i));
        }
        return String.join(" ", rows);
    }

    // ---------------------------------------------------------------------
    // Helpers

    private static <T> void applyRequest(Context ctx, TQuery query) {
        if (ctx == null) {
            return;
        }
        Map<String, String> params = collectParams(ctx);
        if (params.containsKey("Search")) {
            query.Search = params.get("Search");
        }
        if (params.containsKey("Order")) {
            query.Order = params.get("Order");
        }
        if (params.containsKey("Limit")) {
            query.Limit = parseInt(params.get("Limit"), query.Limit);
        }
        if (params.containsKey("Offset")) {
            query.Offset = parseInt(params.get("Offset"), query.Offset);
        }
        Map<Integer, Map<String, String>> filterParams = extractFilterParams(params);
        if (!filterParams.isEmpty()) {
            query.Filter = new ArrayList<>();
            List<Integer> keys = new ArrayList<>(filterParams.keySet());
            keys.sort(Comparator.naturalOrder());
            for (Integer idx : keys) {
                Map<String, String> values = filterParams.get(idx);
                TField field = new TField();
                String db = values.get("Field");
                field.DB = db != null && !db.isEmpty() ? db : values.get("Field");
                field.Field = values.get("Field");
                field.As = parseInt(values.get("As"), field.As);
                field.Condition = values.get("Condition");
                field.Value = values.get("Value");
                field.Bool = parseBool(values.get("Bool"));
                String from = values.get("Dates.From");
                String to = values.get("Dates.To");
                if ((from != null && !from.isEmpty()) || (to != null && !to.isEmpty())) {
                    field.Dates = new TFieldDates();
                    field.Dates.From = parseDate(from);
                    field.Dates.To = parseDate(to);
                }
                query.Filter.add(field);
            }
        }
    }

    private static Map<Integer, Map<String, String>> extractFilterParams(Map<String, String> params) {
        Map<Integer, Map<String, String>> out = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("Filter.")) {
                continue;
            }
            String remaining = key.substring("Filter.".length());
            int dot = remaining.indexOf('.');
            if (dot < 0) {
                continue;
            }
            String indexPart = remaining.substring(0, dot);
            String field = remaining.substring(dot + 1);
            int idx = parseInt(indexPart, -1);
            if (idx < 0) {
                continue;
            }
            Map<String, String> bucket = out.computeIfAbsent(idx, k -> new LinkedHashMap<>());
            bucket.put(field, entry.getValue());
        }
        return out;
    }

    private static Map<String, String> collectParams(Context ctx) {
        Map<String, String> params = new LinkedHashMap<>();
        if (ctx.query != null) {
            params.putAll(ctx.query);
        }
        String contentType = ctx.header("content-type");
        if (ctx.body != null && ctx.body.length > 0) {
            if (contentType == null || contentType.contains("application/x-www-form-urlencoded")) {
                String raw = new String(ctx.body, StandardCharsets.UTF_8);
                Map<String, String> decoded = decodeForm(raw);
                params.putAll(decoded);
            }
        }
        return params;
    }

    private static Map<String, String> decodeForm(String raw) {
        Map<String, String> out = new LinkedHashMap<>();
        if (raw == null || raw.isEmpty()) {
            return out;
        }
        String[] pairs = raw.split("&");
        for (String pair : pairs) {
            if (pair == null || pair.isEmpty()) {
                continue;
            }
            int idx = pair.indexOf('=');
            String key;
            String value;
            if (idx >= 0) {
                key = pair.substring(0, idx);
                value = pair.substring(idx + 1);
            } else {
                key = pair;
                value = "";
            }
            key = urlDecode(key);
            value = urlDecode(value);
            out.put(key, value);
        }
        return out;
    }

    private static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value.replace('+', ' '), StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            return value;
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return value != null && !value.isEmpty() ? Integer.parseInt(value) : fallback;
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static boolean parseBool(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim().toLowerCase(Locale.ROOT);
        return "1".equals(v) || "true".equals(v) || "on".equals(v) || "yes".equals(v);
    }

    private static Date parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(value);
        } catch (ParseException ex) {
            return null;
        }
    }

    private static String formatDate(Date value) {
        if (value == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(value);
    }

    private static <T> TField ensureFilter(TQuery query, int index, TField definition) {
        while (query.Filter.size() <= index) {
            query.Filter.add(new TField());
        }
        TField field = query.Filter.get(index);
        if (field.DB == null || field.DB.isEmpty()) {
            field.DB = definition.DB != null && !definition.DB.isEmpty() ? definition.DB : definition.Field;
        }
        if (field.Field == null || field.Field.isEmpty()) {
            field.Field = definition.Field;
        }
        if (field.Text == null) {
            field.Text = definition.Text;
        }
        if (field.As == 0) {
            field.As = definition.As;
        }
        if ((field.Options == null || field.Options.isEmpty()) && definition.Options != null) {
            field.Options = definition.Options;
        }
        if (field.Dates == null && definition.As == DATES) {
            field.Dates = new TFieldDates();
        }
        return field;
    }

    private static <T> void normalizeQuery(TQuery query, TQuery defaults) {
        if (query.Offset < 0) {
            query.Offset = 0;
        }
        if (query.Limit <= 0) {
            query.Limit = defaults != null && defaults.Limit > 0 ? defaults.Limit : 10;
        }
        if (query.Search == null) {
            query.Search = "";
        }
        if (query.Order == null) {
            query.Order = "";
        }
        if (query.Filter == null) {
            query.Filter = new ArrayList<>();
        }
    }

    private static <T> TQuery copyQuery(TQuery d) {
        TQuery out = new TQuery();
        if (d == null) {
            return out;
        }
        out.Limit = d.Limit;
        out.Offset = d.Offset;
        out.Order = d.Order != null ? d.Order : "";
        out.Search = d.Search != null ? d.Search : "";
        out.Filter = new ArrayList<>();
        if (d.Filter != null) {
            for (TField field : d.Filter) {
                if (field == null) {
                    continue;
                }
                TField copy = new TField();
                copy.DB = field.DB;
                copy.Field = field.Field;
                copy.Text = field.Text;
                copy.Value = field.Value;
                copy.As = field.As;
                copy.Condition = field.Condition;
                copy.Options = field.Options != null ? new ArrayList<>(field.Options) : new ArrayList<>();
                copy.Bool = field.Bool;
                if (field.Dates != null) {
                    copy.Dates = new TFieldDates();
                    copy.Dates.From = field.Dates.From;
                    copy.Dates.To = field.Dates.To;
                }
                out.Filter.add(copy);
            }
        }
        return out;
    }

    private static <T> List<TField> copyFields(List<TField> fields) {
        if (fields == null) {
            return new ArrayList<>();
        }
        List<TField> copy = new ArrayList<>();
        for (TField f : fields) {
            if (f == null) {
                continue;
            }
            TField nf = new TField();
            nf.DB = f.DB;
            nf.Field = f.Field;
            nf.Text = f.Text;
            nf.Value = f.Value;
            nf.As = f.As;
            nf.Condition = f.Condition;
            nf.Options = f.Options != null ? new ArrayList<>(f.Options) : new ArrayList<>();
            nf.Bool = f.Bool;
            if (f.Dates != null) {
                nf.Dates = new TFieldDates();
                nf.Dates.From = f.Dates.From;
                nf.Dates.To = f.Dates.To;
            }
            copy.add(nf);
        }
        return copy;
    }

    private static <T> TQuery makeQuery(TQuery d) {
        if (d == null) {
            d = new TQuery();
        }
        TQuery out = new TQuery();
        out.Limit = d.Limit > 0 ? d.Limit : 10;
        out.Offset = d.Offset >= 0 ? d.Offset : 0;
        out.Order = d.Order != null ? d.Order : "";
        out.Search = d.Search != null ? d.Search : "";
        out.Filter = d.Filter != null ? d.Filter : new ArrayList<>();
        return out;
    }

    private static final class State<T> {
        TQuery Init;
        Ui.Target Target;
        Ui.Target TargetFilter;
        List<TField> SearchFields = new ArrayList<>();
        List<TField> SortFields = new ArrayList<>();
        List<TField> FilterFields = new ArrayList<>();
        List<TField> ExcelFields = new ArrayList<>();
        RenderRow<T> OnRow;
        Export<T> OnExcel;
        Loader<T> Loader;
        Context.Callable ActionSearch;
        Context.Callable ActionSort;
        Context.Callable ActionReset;
        Context.Callable ActionResize;
        Context.Callable ActionExcel;
        boolean Debug = false;
    }
}
