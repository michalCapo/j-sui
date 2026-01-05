package jsui.examples.pages;

import java.util.ArrayList;
import java.util.List;
import jsui.Context;
import jsui.Data;
import jsui.ui;
import jsui.examples.models.CollateRow;
import jsui.examples.models.OrderSpec;

public final class CollatePage {

    public static String render(Context ctx) throws Exception {
        seed();

        Data.TQuery init = new Data.TQuery();
        init.Limit = 10;
        init.Offset = 0;
        init.Order = "createdat desc";
        init.Search = "";
        init.Filter = new ArrayList<>();

        Data.CollateModel<CollateRow> collate = Data.Collate(init, CollatePage::load);
        collate.setFilter(buildFilters());
        collate.setSort(buildSort());
        collate.setExcel(buildExcel());
        collate.Row((row, index) -> renderRow(row));

        String body = ui.div("flex flex-col gap-4").render(
                ui.div("text-3xl font-bold").render("Data Collation"),
                ui.div("text-gray-600 mb-2")
                        .render("Search, sort, filter, and paging over an in-memory dataset of 100 rows."),
                collate.Render(ctx));

        return ui.div("flex flex-col gap-4").render(body);
    }

    private static String renderRow(CollateRow r) {
        String created = new java.text.SimpleDateFormat("yyyy-MM-dd").format(r.CreatedAt);
        return ui.div("bg-white rounded border border-gray-200 p-3 flex items-center gap-3").render(
                ui.div("w-12 text-right font-mono text-gray-500").render("#" + r.ID),
                ui.div("flex-1").render(
                        ui.div("font-semibold").render(
                                r.Name + ui.space
                                        + ui.div("inline text-gray-500 text-sm").render("(" + r.Role + ")")),
                        ui.div("text-gray-600 text-sm").render(r.Email + " · " + r.City)),
                ui.div("text-gray-500 text-sm").render(created),
                ui.div("ml-2").render(
                        ui.Button()
                                .Class("w-20 text-center px-2 py-1 rounded")
                                .Color(r.Active ? ui.Green : ui.Gray)
                                .Render(r.Active ? "Active" : "Inactive")));
    }

    private static Data.LoadResult<CollateRow> load(Data.TQuery query) {
        List<CollateRow> list = new ArrayList<>();
        synchronized (DB) {
            for (CollateRow r : DB) {
                list.add(r.copy());
            }
        }
        int total = list.size();

        String search = query.Search != null ? query.Search.trim() : "";
        if (!search.isEmpty()) {
            String needle = Data.NormalizeForSearch(search);
            list.removeIf(r -> {
                String hay = Data.NormalizeForSearch(r.Name) + " " + Data.NormalizeForSearch(r.Email) + " "
                        + Data.NormalizeForSearch(r.City);
                return !hay.contains(needle);
            });
        }

        if (query.Filter != null) {
            for (Data.TField f : query.Filter) {
                if (f == null)
                    continue;
                if (f.As == Data.BOOL && f.Bool) {
                    list.removeIf(r -> !r.Active);
                } else if (f.As == Data.SELECT && f.Value != null && !f.Value.isEmpty()) {
                    String v = f.Value.toLowerCase();
                    list.removeIf(r -> !r.Role.equalsIgnoreCase(v));
                } else if (f.As == Data.DATES && f.Dates != null) {
                    if (f.Dates.From != null && f.Dates.From.getTime() > 0) {
                        long from = truncateStart(f.Dates.From).getTime();
                        list.removeIf(r -> r.CreatedAt.getTime() < from);
                    }
                    if (f.Dates.To != null && f.Dates.To.getTime() > 0) {
                        long to = truncateEnd(f.Dates.To).getTime();
                        list.removeIf(r -> r.CreatedAt.getTime() > to);
                    }
                }
            }
        }

        sort(list, query.Order != null ? query.Order : "createdat desc");

        int filtered = list.size();
        int offset = query.Offset > 0 ? query.Offset : 0;
        int limit = query.Limit > 0 ? query.Limit : 10;
        if (offset > filtered) {
            offset = 0;
        }
        int toIndex = Math.min(offset + limit, filtered);
        List<CollateRow> page = list.subList(offset, toIndex);

        Data.LoadResult<CollateRow> result = new Data.LoadResult<>();
        result.total = total;
        result.filtered = filtered;
        result.data.addAll(page);
        return result;
    }

    private static void sort(List<CollateRow> list, String order) {
        OrderSpec spec = parseOrder(order, "createdat", "desc");
        list.sort((a, b) -> {
            int cmp;
            switch (spec.Field) {
                case "name":
                    cmp = a.Name.compareToIgnoreCase(b.Name);
                    break;
                case "email":
                    cmp = a.Email.compareToIgnoreCase(b.Email);
                    break;
                case "city":
                    cmp = a.City.compareToIgnoreCase(b.City);
                    break;
                default:
                    cmp = Long.compare(a.CreatedAt.getTime(), b.CreatedAt.getTime());
                    break;
            }
            return "desc".equals(spec.Direction) ? -cmp : cmp;
        });
    }

    private static OrderSpec parseOrder(String s, String defField, String defDir) {
        if (s == null) {
            return new OrderSpec(defField, defDir);
        }
        String txt = s.trim();
        if (txt.isEmpty()) {
            return new OrderSpec(defField, defDir);
        }
        String[] parts = txt.split("\\s+");
        String field = parts.length > 0 ? parts[0].toLowerCase() : defField;
        String dir = parts.length > 1 ? parts[1].toLowerCase() : defDir;
        if (!"asc".equals(dir) && !"desc".equals(dir)) {
            dir = defDir;
        }
        return new OrderSpec(field, dir);
    }

    private static java.util.Date truncateStart(java.util.Date date) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static java.util.Date truncateEnd(java.util.Date date) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    private static List<Data.TField> buildFilters() {
        List<Data.TField> fields = new ArrayList<>();

        Data.TField active = new Data.TField();
        active.DB = "Active";
        active.Field = "Active";
        active.Text = "Active";
        active.Value = "";
        active.As = Data.BOOL;
        active.Options = new ArrayList<>();
        active.Bool = false;
        active.Dates = new Data.TFieldDates();
        fields.add(active);

        Data.TField created = new Data.TField();
        created.DB = "CreatedAt";
        created.Field = "CreatedAt";
        created.Text = "Created";
        created.As = Data.DATES;
        created.Dates = new Data.TFieldDates();
        fields.add(created);

        Data.TField role = new Data.TField();
        role.DB = "Role";
        role.Field = "Role";
        role.Text = "Role";
        role.As = Data.SELECT;
        role.Options = List.of(
                new ui.AOption("", "All"),
                new ui.AOption("user", "User"),
                new ui.AOption("admin", "Admin"),
                new ui.AOption("manager", "Manager"),
                new ui.AOption("support", "Support"));
        role.Dates = new Data.TFieldDates();
        fields.add(role);

        return fields;
    }

    private static List<Data.TField> buildSort() {
        List<Data.TField> fields = new ArrayList<>();

        Data.TField name = new Data.TField();
        name.DB = "Name";
        name.Field = "Name";
        name.Text = "Name";
        fields.add(name);

        Data.TField email = new Data.TField();
        email.DB = "Email";
        email.Field = "Email";
        email.Text = "Email";
        fields.add(email);

        Data.TField city = new Data.TField();
        city.DB = "City";
        city.Field = "City";
        city.Text = "City";
        fields.add(city);

        Data.TField created = new Data.TField();
        created.DB = "CreatedAt";
        created.Field = "CreatedAt";
        created.Text = "Created";
        fields.add(created);

        return fields;
    }

    private static List<Data.TField> buildExcel() {
        List<Data.TField> fields = new ArrayList<>();
        Data.TField id = new Data.TField();
        id.DB = "ID";
        id.Field = "ID";
        id.Text = "#";
        fields.add(id);
        Data.TField name = new Data.TField();
        name.DB = "Name";
        name.Field = "Name";
        name.Text = "Name";
        fields.add(name);
        Data.TField email = new Data.TField();
        email.DB = "Email";
        email.Field = "Email";
        email.Text = "Email";
        fields.add(email);
        Data.TField city = new Data.TField();
        city.DB = "City";
        city.Field = "City";
        city.Text = "City";
        fields.add(city);
        Data.TField role = new Data.TField();
        role.DB = "Role";
        role.Field = "Role";
        role.Text = "Role";
        fields.add(role);
        Data.TField active = new Data.TField();
        active.DB = "Active";
        active.Field = "Active";
        active.Text = "Active";
        fields.add(active);
        Data.TField created = new Data.TField();
        created.DB = "CreatedAt";
        created.Field = "CreatedAt";
        created.Text = "Created";
        fields.add(created);
        return fields;
    }

    private static void seed() {
        synchronized (DB) {
            if (SEEDED) {
                return;
            }
            String[] firstNames = { "John", "Jane", "Alex", "Emily", "Michael", "Sarah", "David", "Laura", "Chris",
                    "Anna", "Robert", "Julia", "Daniel", "Mia", "Peter", "Sophia" };
            String[] lastNames = { "Smith", "Johnson", "Brown", "Williams", "Jones", "Garcia", "Miller", "Davis",
                    "Martinez", "Lopez", "Taylor", "Anderson", "Thomas", "Harris", "Clark", "Lewis" };
            String[] cities = { "New York", "San Francisco", "London", "Berlin", "Paris", "Madrid", "Prague", "Tokyo",
                    "Sydney", "Toronto", "Dublin", "Vienna", "Oslo", "Copenhagen", "Warsaw", "Lisbon" };
            String[] roles = { "user", "admin", "manager", "support" };
            String[] domains = { "example.com", "mail.com", "corp.local", "dev.io" };

            java.util.concurrent.ThreadLocalRandom rnd = java.util.concurrent.ThreadLocalRandom.current();
            for (int i = 0; i < 100; i++) {
                String fn = firstNames[rnd.nextInt(firstNames.length)];
                String ln = lastNames[rnd.nextInt(lastNames.length)];
                CollateRow row = new CollateRow();
                row.ID = i + 1;
                row.Name = fn + " " + ln;
                row.City = cities[rnd.nextInt(cities.length)];
                row.Role = roles[rnd.nextInt(roles.length)];
                row.Active = rnd.nextDouble() < 0.62;
                String dom = domains[rnd.nextInt(domains.length)];
                row.Email = fn.toLowerCase() + "." + ln.toLowerCase() + "@" + dom;
                long now = System.currentTimeMillis();
                long days = rnd.nextInt(0, 365);
                row.CreatedAt = new java.util.Date(now - days * 24L * 60L * 60L * 1000L);
                DB.add(row);
            }
            SEEDED = true;
        }
    }

    private static final List<CollateRow> DB = new ArrayList<>();
    private static boolean SEEDED = false;
}
