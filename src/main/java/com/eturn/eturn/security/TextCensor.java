package com.eturn.eturn.security;

import java.util.*;

import java.util.regex.*;

import java.util.regex.Pattern;

public class TextCensor {
    private static final Pattern BAD_WORDS_PATTERN;

    static {
        String regex = buildCompleteRegex();
        BAD_WORDS_PATTERN = Pattern.compile(regex, Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE | Pattern.COMMENTS);
    }

    private static String buildCompleteRegex() {
        String pretext = buildPretextRegex();
        String[] badWords = {
                // Хуй и производные
                "(?<![^\\s\\d])(?:" + pretext + ")?+[hхx]\\s*[уyu]\\s*[ийiеeёяюju](?<!_hue(?=_)|_хуе(?=дин)|_hyu(?=ndai_))",

                // Пизда и производные
                "(?<![^\\s\\d])(?:" + pretext + ")?+[пp]\\s*[иieеё]\\s*[зz3]\\s*[дd](?=\\s*[аеиоуыэюяёaeioyu])",

                // Еб и производные
                "(?<![^\\s\\d])(?:" + pretext + ")?+[eеё]\\s*[бb6](?=\\s*([уyиi]|"
                        + "[ыиiоoaаеeёуy]\\s*[^аеиоуыэюяёaeioyu\\s\\d]|"
                        + "[лl]([оoаaыиiя]|ya)|"
                        + "[нn]\\s*[уy]|"
                        + "[кk]\\s*[аa]|"
                        + "[сc]\\s*[тt]))",

                // Еб с приставкой
                "(?<![^\\s\\d])(?:" + pretext + ")?+(?<=\\p{L}{2,})[eеё]\\s*[бb6]",

                // Ёб
                "(?<![^\\s\\d])ёб(?=\\s*[^\\s\\d])",

                // Бля и производные
                "(?<![^\\s\\d])(?:" + pretext + ")?+[бb6]\\s*[лl]\\s*(?:я|ya)(?:\\s*[тдtd])?",

                // Пидор и производные
                "[пp]\\s*[иieе]\\s*[дdg]\\s*[eеaаoо]\\s*[rpр]",

                // Мудак и производные
                "[мm]\\s*[уy]\\s*[дdg]\\s*[аа](?<!_myda(?=s_))",

                // Жопа и производные
                "[zж]\\s*[оo]\\s*[pп]\\s*[aаyуыiеeoо]",

                // Манда и производные
                "[мm]\\s*[аa]\\s*[нnh]\\s*[дdg]\\s*[aаyуыiеeoо](?<!манда(?=[лн]|рин))",

                // Говно и производные
                "[гg]\\s*[оo]\\s*[вvb]\\s*[нnh]\\s*[оoаaяеeyу]",

                // Fuck и производные
                "f\\s*u\\s*[cс]\\s*k"
        };

        return "(?xi)(" + String.join("|", badWords) + ")";
    }

    private static String buildPretextRegex() {
        String[] prefixes = {
                "[уyоoаa]\\s*(?=[еёeхx])",        // у, о, а (уебать, охуеть, ахуеть)
                "[вvbсc]\\s*(?=[хпбмгжxpmgj])",   // в, с (впиздячить, схуярить)
                "[вvbсc]\\s*[ъь]\\s*(?=[еёe])",   // въ, съ (съебаться, въебать)
                "ё\\s*(?=[бb6])",                 // ё (ёбля)
                "[вvb]\\s*[ыi]",                  // вы
                "[зz3]\\s*[аa]",                  // за
                "[нnh]\\s*[аaеeиi]",              // на, не, ни
                "[вvb]\\s*[сc]\\s*(?=[хпбмгжxpmgj])",  // вс (вспизднуть)
                "[оo]\\s*[тtбb6]\\s*(?=[хпбмгжxpmgj])",  // от, об
                "[оo]\\s*[тtбb6]\\s*[ъь]\\s*(?=[еёe])",    // отъ, объ
                "[иiвvb]\\s*[зz3]\\s*(?=[хпбмгжxpmgj])",  // [ив]з
                "[иiвvb]\\s*[зz3]\\s*[ъь]\\s*(?=[еёe])",    // [ив]зъ
                "[иi]\\s*[сc]\\s*(?=[хпбмгжxpmgj])",      // ис
                "[пpдdg]\\s*[оo]\\s*(?>[бb6]\\s*(?=[хпбмгжxpmgj])|[бb6]\\s*[ъь]\\s*(?=[еёe])|[зz3]\\s*[аa])?",  // по, до, пообъ, дообъ, поза, доза
                "[пp]\\s*[рr]\\s*[оoиi]",  // пр[ои]
                "[зz3]\\s*[лl]\\s*[оo]",   // зло (злоебучая)
                "[нnh]\\s*[аa]\\s*[дdg]\\s*(?=[хпбмгжxpmgj])",  // над
                "[нnh]\\s*[аa]\\s*[дdg]\\s*[ъь]\\s*(?=[еёe])",    // надъ
                "[пp]\\s*[оoаa]\\s*[дdg]\\s*(?=[хпбмгжxpmgj])",  // под
                "[пp]\\s*[оoаa]\\s*[дdg]\\s*[ъь]\\s*(?=[еёe])",    // подъ
                "[рr]\\s*[аa]\\s*[зz3сc]\\s*(?=[хпбмгжxpmgj])",  // ра[зс]
                "[рr]\\s*[аa]\\s*[зz3сc]\\s*[ъь]\\s*(?=[еёe])",    // ра[зс]ъ
                "[вvb]\\s*[оo]\\s*[зz3сc]\\s*(?=[хпбмгжxpmgj])",  // во[зс]
                "[вvb]\\s*[оo]\\s*[зz3сc]\\s*[ъь]\\s*(?=[еёe])",    // во[зс]ъ
                "[нnh]\\s*[еe]\\s*[дdg]\\s*[оo]",    // недо
                "[пp]\\s*[еe]\\s*[рr]\\s*[еe]",      // пере
                "[oо]\\s*[дdg]\\s*[нnh]\\s*[оo]",    // одно
                "[кk]\\s*[oо]\\s*[нnh]\\s*[оo]",     // коно (коноебиться)
                "[мm]\\s*[уy]\\s*[дdg]\\s*[oоaа]",   // муд[оа] (мудаёб)
                "[oо]\\s*[сc]\\s*[тt]\\s*[оo]",      // осто (остопиздело)
                "[дdg]\\s*[уy]\\s*[рpr]\\s*[оoаa]",  // дур[оа]
                "[хx]\\s*[уy]\\s*[дdg]\\s*[оoаa]",   // худ[оа] (худоебина)
                "[мm]\\s*[нnh]\\s*[оo]\\s*[гg]\\s*[оo]",    // много
                "[мm]\\s*[оo]\\s*[рpr]\\s*[дdg]\\s*[оoаa]", // морд[оа]
                "[мm]\\s*[оo]\\s*[зz3]\\s*[гg]\\s*[оoаa]",  // мозг[оа]
                "[дdg]\\s*[оo]\\s*[лl]\\s*[бb6]\\s*[оoаa]", // долб[оа]
                "[оo]\\s*[сc]\\s*[тt]\\s*[рpr]\\s*[оo]"    // остро
        };
        return "(?:" + String.join("|", prefixes) + ")?+";
    }

    public static boolean parse(String text) {
        if (text == null) return true;

        String processed = preprocessText(text);
        return !BAD_WORDS_PATTERN.matcher(processed).find();
    }

    private static String preprocessText(String text) {
        return text.toLowerCase()
                .replaceAll("<[^>]+>", "") // HTML теги
                .replaceAll("&[^;]+;", "") // HTML сущности
                .replaceAll("[^а-яёa-z0-9ъь]", " ")
                .replaceAll("/\\\\", "л")
                .replaceAll("[@3]", "з")
                .replaceAll("[6]", "б")
                .replaceAll("[0]", "о")
                .replaceAll("\\s+", " ");
    }

    public static void main(String[] args) {
        System.out.println(parse("подъебать"));     // false
        System.out.println(parse("хуй"));        // false
        System.out.println(parse("схуярить"));   // false
        System.out.println(parse("пиздёж"));     // false
        System.out.println(parse("ебёт"));       // false

        // Должны возвращать true
        System.out.println(parse("hyundai"));    // true (исключение)
        System.out.println(parse("мандарин"));   // true (исключение)
        System.out.println(parse("hue цвет"));   // true (исключение)
    }
}
