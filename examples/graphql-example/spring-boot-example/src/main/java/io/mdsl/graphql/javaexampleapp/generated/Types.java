package io.mdsl.graphql.javaexampleapp.generated;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

public class Types {
  public static class PaperItemDtoInput {
    private String _title;
    private String _authors;
    private String _venue;
    private PaperItemKeyInput _paperItemId;
  
    public PaperItemDtoInput(Map<String, Object> args) {
      if (args != null) {
        this._title = (String) args.get("title");
        this._authors = (String) args.get("authors");
        this._venue = (String) args.get("venue");
        this._paperItemId = new PaperItemKeyInput((Map<String, Object>) args.get("paperItemId"));
      }
    }
  
    public String getTitle() { return this._title; }
    public String getAuthors() { return this._authors; }
    public String getVenue() { return this._venue; }
    public PaperItemKeyInput getPaperItemId() { return this._paperItemId; }
  }
  public static class PaperItemKeyInput {
    private String _doi;
  
    public PaperItemKeyInput(Map<String, Object> args) {
      if (args != null) {
        this._doi = (String) args.get("doi");
      }
    }
  
    public String getDoi() { return this._doi; }
  }
  public static class CreatePaperInput {
    private String _who;
    private String _what;
    private String _where;
  
    public CreatePaperInput(Map<String, Object> args) {
      if (args != null) {
        this._who = (String) args.get("who");
        this._what = (String) args.get("what");
        this._where = (String) args.get("where");
      }
    }
  
    public String getWho() { return this._who; }
    public String getWhat() { return this._what; }
    public String getWhere() { return this._where; }
  }
  public static class LookupPapersFromAuthorRequestDataTypeInput {
    private String _anonymous1;
  
    public LookupPapersFromAuthorRequestDataTypeInput(Map<String, Object> args) {
      if (args != null) {
        this._anonymous1 = (String) args.get("anonymous1");
      }
    }
  
    public String getAnonymous1() { return this._anonymous1; }
  }
  public static class PaperItemDtoListInput {
    private Iterable<PaperItemDtoInput> _entries;
  
    public PaperItemDtoListInput(Map<String, Object> args) {
      if (args != null) {
        if (args.get("entries") != null) {
          this._entries = ((List<Map<String, Object>>) args.get("entries")).stream().map(PaperItemDtoInput::new).collect(Collectors.toList());
        }
      }
    }
  
    public Iterable<PaperItemDtoInput> getEntries() { return this._entries; }
  }
  public static class ConvertToMarkdownForWebsiteResponseDataTypeInput {
    private String _anonymous2;
  
    public ConvertToMarkdownForWebsiteResponseDataTypeInput(Map<String, Object> args) {
      if (args != null) {
        this._anonymous2 = (String) args.get("anonymous2");
      }
    }
  
    public String getAnonymous2() { return this._anonymous2; }
  }
  
  
  
  
  
  
  public static class QueryLookupPapersFromAuthorArgs {
    private LookupPapersFromAuthorRequestDataTypeInput _anonymousInput;
  
    public QueryLookupPapersFromAuthorArgs(Map<String, Object> args) {
      if (args != null) {
        this._anonymousInput = new LookupPapersFromAuthorRequestDataTypeInput((Map<String, Object>) args.get("anonymousInput"));
      }
    }
  
    public LookupPapersFromAuthorRequestDataTypeInput getAnonymousInput() { return this._anonymousInput; }
  }
  public static class MutationCreatePaperItemArgs {
    private CreatePaperInput _anonymousInput;
  
    public MutationCreatePaperItemArgs(Map<String, Object> args) {
      if (args != null) {
        this._anonymousInput = new CreatePaperInput((Map<String, Object>) args.get("anonymousInput"));
      }
    }
  
    public CreatePaperInput getAnonymousInput() { return this._anonymousInput; }
  }
  public static class MutationConvertToMarkdownForWebsiteArgs {
    private PaperItemKeyInput _anonymousInput;
  
    public MutationConvertToMarkdownForWebsiteArgs(Map<String, Object> args) {
      if (args != null) {
        this._anonymousInput = new PaperItemKeyInput((Map<String, Object>) args.get("anonymousInput"));
      }
    }
  
    public PaperItemKeyInput getAnonymousInput() { return this._anonymousInput; }
  }
}
