package jsui;

import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Java port of key data helpers from t-sui/ui.data.ts.
 *
 * Provides NormalizeForSearch, table/filtering models, and a Collate UI
 * component that mirrors the TypeScript behaviour using only JDK classes.
 */
public final class Data {
    private Data() {
    }

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

    public static final int BOOL = 0;
    public static final int NOT_ZERO_DATE = 1;
    public static final int ZERO_DATE = 2;
    public static final int DATES = 3;
    public static final int SELECT = 4;

    public static final List<ui.AOption> BOOL_ZERO_OPTIONS;
    static {
        List<ui.AOption> opts = new ArrayList<>();
        opts.add(new ui.AOption("", "All"));
        opts.add(new ui.AOption("yes", "On"));
        opts.add(new ui.AOption("no", "Off"));
        BOOL_ZERO_OPTIONS = Collections.unmodifiableList(opts);
    }

    @lombok.Data
    public static final class TFieldDates {
        public Date From;
        public Date To;
    }

    @lombok.Data
    public static final class TField {
        public String DB;
        public String Field;
        public String Text;

        public String Value;
        public int As;
        public String Condition;
        public List<ui.AOption> Options = new ArrayList<>();

        public boolean Bool;
        public TFieldDates Dates;
    }

    @lombok.Data
    public static final class TQuery {
        public int Limit;
        public int Offset;
        public String Order = "";
        public String Search = "";
        public List<TField> Filter = new ArrayList<>();
    }

    @lombok.Data
    public static final class TCollateResult<T> {
        public int Total;
        public int Filtered;
        public List<T> Data = new ArrayList<>();
        public TQuery Query;
    }

    @lombok.Data
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

    public static <T> CollateModel<T> Collate(TQuery init, Loader<T> loader) {
        final State<T> state = new State<>();
        state.Init = makeQuery(init);
        state.Target = ui.Target();
        state.TargetFilter = ui.Target();
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
            if (query.Limit <= 0)
                query.Limit = 1000000;
            query.Offset = 0;
            normalizeQuery(query, state.Init);
            TCollateResult<T> result = triggerLoad(state, query);
            if (result == null || result.Data == null || result.Data.isEmpty()) {
                ctx.Info("No data to export.");
                return "";
            }

            List<TField> headers = state.ExcelFields != null && !state.ExcelFields.isEmpty()
                    ? state.ExcelFields
                    : (state.SortFields != null && !state.SortFields.isEmpty() ? state.SortFields : state.FilterFields);

            List<String> headerNames = new ArrayList<>();
            if (headers != null && !headers.isEmpty()) {
                for (TField h : headers) {
                    String name = (h != null && h.Text != null && !h.Text.isEmpty()) ? h.Text
                            : (h != null ? (h.Field != null ? h.Field : h.DB) : "");
                    headerNames.add(name != null ? name : "");
                }
            } else {
                if (!result.Data.isEmpty()) {
                    for (java.lang.reflect.Field f : result.Data.get(0).getClass().getFields()) {
                        headerNames.add(f.getName());
                    }
                }
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Sheet1");

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headerNames.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headerNames.get(i));
                cell.setCellStyle(headerStyle);
            }

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd"));

            int rowIndex = 1;
            for (T item : result.Data) {
                Row row = sheet.createRow(rowIndex++);
                int colIndex = 0;

                if (headers != null && !headers.isEmpty()) {
                    for (TField h : headers) {
                        String field = h != null && h.Field != null && !h.Field.isEmpty() ? h.Field
                                : (h != null ? h.DB : null);
                        Object value = getFieldValue(item, field);
                        Cell cell = row.createCell(colIndex++);
                        setCellValue(cell, value, dateStyle);
                    }
                } else {
                    for (java.lang.reflect.Field f : item.getClass().getFields()) {
                        Object value = get(item, f);
                        Cell cell = row.createCell(colIndex++);
                        setCellValue(cell, value, dateStyle);
                    }
                }
            }

            for (int i = 0; i < headerNames.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            byte[] excelBytes = outputStream.toByteArray();
            String filename = "export_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";

            ctx.DownloadAs(excelBytes, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename);
            return "";
        } catch (Exception ex) {
            ctxError(state, ex);
            ctx.Error("Export failed: " + ex.getMessage());
            return "";
        }
    }

    private static <T> Object getFieldValue(T item, String fieldName) {
        if (item == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        }
        try {
            java.lang.reflect.Field f = item.getClass().getField(fieldName);
            return f.get(item);
        } catch (NoSuchFieldException e) {
            try {
                java.lang.reflect.Field f = item.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                return f.get(item);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                return null;
            }
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static void setCellValue(Cell cell, Object value, CellStyle dateStyle) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            if (value instanceof Double) {
                cell.setCellValue((Double) value);
            } else if (value instanceof Float) {
                cell.setCellValue((Float) value);
            } else if (value instanceof Long) {
                cell.setCellValue((Long) value);
            } else if (value instanceof Integer) {
                cell.setCellValue((Integer) value);
            } else {
                cell.setCellValue(((Number) value).doubleValue());
            }
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
            cell.setCellStyle(dateStyle);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    private static <T> Object get(T item, java.lang.reflect.Field f) {
        try {
            return f.get(item);
        } catch (IllegalAccessException e) {
            return null;
        }
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
        if (state != null && state.Debug && ex != null) {
            ex.printStackTrace();
        }
    }

    private static <T> String renderUI(Context ctx, State<T> state, TQuery query, TCollateResult<T> result,
            boolean loading) {
        String header = renderHeader(ctx, state, query, loading);
        if (loading || result == null) {
            String skeletonRows = ui.Skeleton.List(ui.Target(), 6);
            String skeletonPager = ui.div("flex items-center justify-center").render(
                    ui.div("mx-4 font-bold text-lg").render("\u00A0"),
                    ui.div("flex gap-px flex-1 justify-end").render(
                            ui.div("bg-gray-200 h-9 w-10 rounded-l border").render(),
                            ui.div("bg-gray-200 h-9 w-36 rounded-r border").render()));
            return ui.div("flex flex-col gap-2 mt-2", state.Target.id()).render(header, skeletonRows,
                    skeletonPager);
        }

        String rows = renderRows(result.Data, state.OnRow);
        String pager = renderPager(ctx, state, result);
        return ui.div("flex flex-col gap-2 mt-2", state.Target.id()).render(header, rows, pager);
    }

    private static <T> String renderHeader(Context ctx, State<T> state, TQuery query, boolean loading) {
        String sorting = renderSorting(ctx, state, query);
        String searching = renderSearching(ctx, state, query);
        String filtering = renderFiltering(ctx, state, query);
        String wrapperCls = loading ? "flex flex-col pointer-events-none" : "flex flex-col";
        return ui.div(wrapperCls).render(
                ui.div("flex gap-x-2").render(
                        sorting,
                        ui.Flex1,
                        searching),
                ui.div("flex justify-end").render(filtering));
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
            String color = ui.GrayOutline;
            if (order.startsWith(field + " ") || order.equals(field)) {
                direction = order.contains("asc") ? "asc" : "desc";
                color = ui.PurpleOutline;
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
            String button = new ui.Button()
                    .Submit()
                    .Class("bg-white rounded")
                    .Color(color)
                    .Render(
                            ui.div("flex gap-2 items-center").render(
                                    directionIcon(direction),
                                    sort.Text != null ? sort.Text : db));
            children.add(button);
            String form = ui.form("inline-flex", ctx.Submit(state.ActionSort).Replace(state.Target.id()))
                    .render(children.toArray(new String[0]));
            buttons.add(form);
        }
        return ui.div("flex gap-1").render(buttons.toArray(new String[0]));
    }

    private static String directionIcon(String direction) {
        if ("asc".equals(direction)) {
            return ui.Icon("fa fa-fw fa-sort-amount-asc");
        }
        if ("desc".equals(direction)) {
            return ui.Icon("fa fa-fw fa-sort-amount-desc");
        }
        return ui.Icon("fa fa-fw fa-sort");
    }

    private static <T> String renderSearching(Context ctx, State<T> state, TQuery query) {
        List<String> children = new ArrayList<>();
        String clearJs = "(function(b){try{var f=b.closest('form');if(!f)return;var i=f.querySelector(\"[name='Search']\");if(i){i.value='';}f.submit();}catch(_){}})(this)";
        String form = ui.form("flex", ctx.Submit(state.ActionSearch).Replace(state.Target.id())).render(
                ui.div("relative flex-1 w-72").render(
                        ui.div("absolute left-3 top-1/2 transform -translate-y-1/2").render(
                                new ui.Button()
                                        .Submit()
                                        .Class("rounded-full bg-white hover:bg-gray-100 h-8 w-8 border border-gray-300 flex items-center justify-center")
                                        .Render(ui.Icon("fa fa-fw fa-search"))),
                        ui.IText("Search", query)
                                .Class("p-1 w-full")
                                .ClassInput(
                                        "cursor-pointer bg-white border-gray-300 hover:border-blue-500 block w-full py-3 pl-12 pr-12")
                                .Placeholder("Search")
                                .Render(""),
                        (query.Search != null && !query.Search.isEmpty())
                                ? ui.div("absolute right-3 top-1/2 transform -translate-y-1/2").render(
                                        new ui.Button()
                                                .Class("rounded-full bg-white hover:bg-gray-100 h-8 w-8 border border-gray-300 flex items-center justify-center")
                                                .Click(clearJs)
                                                .Render(ui.Icon("fa fa-fw fa-times")))
                                : ""));
        children.add(form);

        if (state.ExcelFields != null && !state.ExcelFields.isEmpty()) {
            String excel = new ui.Button()
                    .Color(ui.Blue)
                    .Class("rounded-lg shadow px-4 h-12 flex items-center gap-2")
                    .Click(ctx.Call(state.ActionExcel).None())
                    .Render(ui.IconLeft("fa fa-download", "XLS"));
            children.add(excel);
        }

        if (state.FilterFields != null && !state.FilterFields.isEmpty()) {
            String toggle = new ui.Button()
                    .Class("rounded-r-lg shadow h-12 px-4 flex items-center gap-2")
                    .Color(ui.Blue)
                    .Click("var el=document.getElementById('" + state.TargetFilter.id()
                            + "'); if(el){el.classList.toggle('hidden');}")
                    .Render(ui.IconLeft("fa fa-fw fa-chevron-down", "Filter"));
            children.add(toggle);
        }

        return ui.div("flex gap-px bg-blue-800 rounded-lg p-1 items-center").render(children.toArray(new String[0]));
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
                parts.add(ui.Hidden(position + ".Field", "string", target.DB));
                parts.add(ui.Hidden(position + ".As", "number", Integer.toString(def.As)));
                parts.add(hiddenCheckbox(position + ".Bool", target.Bool));
                parts.add(ui.ICheckbox(position + ".Bool", query).Render(def.Text));
            } else if (def.As == DATES) {
                parts.add(ui.Hidden(position + ".Field", "string", target.DB));
                parts.add(ui.Hidden(position + ".As", "number", Integer.toString(def.As)));
                parts.add(ui.IDate(position + ".Dates.From", query).Render("From"));
                parts.add(ui.IDate(position + ".Dates.To", query).Render("To"));
            } else if (def.As == SELECT) {
                parts.add(ui.Hidden(position + ".Field", "string", target.DB));
                parts.add(ui.Hidden(position + ".As", "number", Integer.toString(def.As)));
                ui.ISelect select = ui.ISelect(position + ".Value", query)
                        .Options(target.Options != null && !target.Options.isEmpty() ? target.Options : def.Options)
                        .Value(target.Value != null ? target.Value : "");
                parts.add(select.Render(def.Text));
            } else if (def.As == BOOL) {
                parts.add(ui.Hidden(position + ".Field", "string", target.DB));
                parts.add(ui.Hidden(position + ".As", "number", Integer.toString(def.As)));
                parts.add(ui.Hidden(position + ".Condition", "string",
                        target.Condition != null ? target.Condition : def.Condition));
                parts.add(hiddenCheckbox(position + ".Bool", target.Bool));
                parts.add(ui.ICheckbox(position + ".Bool", query).Render(def.Text));
            }
            rows.add(ui.div("col-span-2 flex flex-col gap-2").render(parts.toArray(new String[0])));
        }

        List<String> children = new ArrayList<>();
        children.add(ui.Hidden("Search", "string", query.Search));
        children.add(ui.Hidden("Order", "string", query.Order));
        children.add(ui.Hidden("Limit", "number", Integer.toString(query.Limit)));
        children.add(ui.Hidden("Offset", "number", "0"));
        children.addAll(rows);

        String buttons = ui.div("flex justify-end gap-2 mt-6 pt-3 border-t border-gray-200").render(
                new ui.Button()
                        .Submit()
                        .Class("rounded-full h-10 px-4 bg-white")
                        .Color(ui.GrayOutline)
                        .Click("return false")
                        .Render(ui.IconLeft("fa fa-fw fa-rotate-left", "Reset")),
                new ui.Button()
                        .Submit()
                        .Class("rounded-full h-10 px-4 shadow")
                        .Color(ui.Blue)
                        .Render(ui.IconLeft("fa fa-fw fa-check", "Apply")));
        children.add(buttons);

        String form = ui.form("flex flex-col p-4", ctx.Submit(state.ActionSearch).Replace(state.Target.id()))
                .render(children.toArray(new String[0]));
        return ui.div("col-span-2 relative h-0 hidden z-30", state.TargetFilter.id()).render(
                ui.div("absolute top-2 right-0 w-96 bg-white rounded-xl shadow-xl ring-1 ring-black/10 border border-gray-200")
                        .render(form));
    }

    private static String resetFiltersJs(String targetId) {
        return "(function(btn){try{var form=btn.closest('form');if(!form)return;var fields=form.querySelectorAll(\"[name^='Filter.']\");for(var i=0;i<fields.length;i++){var el=fields[i];if(!el)continue;var type=(el.getAttribute('type')||'').toLowerCase();if(type==='checkbox'){el.checked=false;}else{el.value='';}}}catch(_){}})(this)";
    }

    private static String hiddenCheckbox(String name, boolean value) {
        return ui.input("", ui.Attr.of().type("hidden").name(name).value(Boolean.toString(value)));
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
        return ui.input("", ui.Attr.of().type("hidden").name(name).value(value != null ? value : ""));
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
        resetChildren.add(new ui.Button()
                .Submit()
                .Class("bg-white rounded-l h-10 px-4")
                .Color(ui.PurpleOutline)
                .Render(ui.Icon("fa fa-fw fa-undo")));
        String resetForm = ui.form("inline-flex", ctx.Submit(state.ActionReset).Replace(state.Target.id()))
                .render(resetChildren.toArray(new String[0]));

        List<String> moreChildren = new ArrayList<>();
        moreChildren.add(hiddenInput("Search", result.Query != null ? result.Query.Search : ""));
        moreChildren.add(hiddenInput("Order", result.Query != null ? result.Query.Order : ""));
        moreChildren.add(hiddenInput("Limit", Integer.toString(result.Query != null ? result.Query.Limit : 10)));
        moreChildren.add(hiddenInput("Offset", Integer.toString(result.Query != null ? result.Query.Offset : 0)));
        moreChildren.addAll(hiddenFilterInputs(result.Query != null ? result.Query : new TQuery()));
        moreChildren.add(new ui.Button()
                .Submit()
                .Class("rounded-r h-10 px-4")
                .Color(ui.Purple)
                .Disabled(size >= result.Filtered)
                .Render(ui.div("flex gap-2 items-center").render(
                        ui.Icon("fa fa-arrow-down"),
                        "Load more items")));
        String moreForm = ui.form("inline-flex", ctx.Submit(state.ActionResize).Replace(state.Target.id()))
                .render(moreChildren.toArray(new String[0]));

        return ui.div("flex items-center justify-center").render(
                ui.div("mx-4 font-bold text-lg").render(count),
                ui.div("flex gap-px flex-1 justify-end").render(resetForm, moreForm));
    }

    private static <T> String emptyState(TCollateResult<T> result) {
        if (result.Total == 0) {
            return ui.div("mt-2 py-24 rounded text-xl flex justify-center items-center bg-white rounded-lg").render(
                    ui.div("").render(
                            ui.div("text-black text-2xl p-4 mb-2 font-bold flex justify-center items-center")
                                    .render("No records found")));
        }
        return ui.div("mt-2 py-24 rounded text-xl flex justify-center items-center bg-white rounded-lg").render(
                ui.div("flex gap-x-px items-center justify-center text-2xl").render(
                        ui.Icon("fa fa-fw fa-exclamation-triangle text-yellow-500"),
                        ui.div("text-black p-4 mb-2 font-bold flex justify-center items-center")
                                .render("No records found for the selected filter")));
    }

    private static <T> String renderRows(List<T> data, RenderRow<T> onRow) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        if (onRow == null) {
            return ui.div("").render("Missing row renderer");
        }
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            rows.add(onRow.render(data.get(i), i));
        }
        return String.join(" ", rows);
    }

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

    @lombok.Data
    private static final class State<T> {
        TQuery Init;
        ui.Target Target;
        ui.Target TargetFilter;
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
