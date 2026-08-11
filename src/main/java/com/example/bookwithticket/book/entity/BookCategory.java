package com.example.bookwithticket.book.entity;

import java.util.Arrays;

public enum BookCategory {

    NOVEL("소설"),
    POETRY_ESSAY("시/에세이"),
    HUMANITIES("인문"),
    ECONOMY_BUSINESS("경제/경영"),
    SELF_DEVELOPMENT("자기계발"),
    POLITICS_SOCIETY("정치/사회"),
    HISTORY_CULTURE("역사/문화"),
    SCIENCE("과학"),
    IT("컴퓨터/IT"),
    TRAVEL("여행"),
    CHILDREN("어린이"),
    COMIC("만화"),
    FOREIGN_LANGUAGE("외국어"),
    EXAM("수험서"),
    ETC("기타");

    private final String label;

    BookCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static BookCategory from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return Arrays.stream(values())
                .filter(category ->
                        category.name().equalsIgnoreCase(normalized)
                                || category.label.equals(normalized))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("지원하지 않는 도서 카테고리입니다: " + value)
                );
    }

    public static BookCategory fromAladinCategoryName(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return ETC;
        }

        String value = categoryName.replace(" ", "");

        if (containsAny(value, "소설", "장르문학")) return NOVEL;
        if (containsAny(value, "시", "에세이")) return POETRY_ESSAY;
        if (containsAny(value, "인문")) return HUMANITIES;
        if (containsAny(value, "경제", "경영")) return ECONOMY_BUSINESS;
        if (containsAny(value, "자기계발")) return SELF_DEVELOPMENT;
        if (containsAny(value, "정치", "사회")) return POLITICS_SOCIETY;
        if (containsAny(value, "역사", "문화")) return HISTORY_CULTURE;
        if (containsAny(value, "과학")) return SCIENCE;
        if (containsAny(value, "컴퓨터", "IT", "모바일", "프로그래밍")) return IT;
        if (containsAny(value, "여행")) return TRAVEL;
        if (containsAny(value, "어린이", "유아")) return CHILDREN;
        if (containsAny(value, "만화")) return COMIC;
        if (containsAny(value, "외국어")) return FOREIGN_LANGUAGE;
        if (containsAny(value, "수험", "자격증")) return EXAM;

        return ETC;
    }

    private static boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
