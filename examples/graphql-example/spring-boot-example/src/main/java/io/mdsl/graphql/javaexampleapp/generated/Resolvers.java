package io.mdsl.graphql.javaexampleapp.generated;

import graphql.schema.DataFetcher;

public class Resolvers {
  public interface PaperItemDtoOutput {
    public DataFetcher<String> title();
    public DataFetcher<String> authors();
    public DataFetcher<String> venue();
    public DataFetcher<Object> paperItemId();
  }
  
  public interface PaperItemKeyOutput {
    public DataFetcher<String> doi();
  }
  
  public interface CreatePaperOutput {
    public DataFetcher<String> who();
    public DataFetcher<String> what();
    public DataFetcher<String> where();
  }
  
  public interface LookupPapersFromAuthorRequestDataTypeOutput {
    public DataFetcher<String> anonymous1();
  }
  
  public interface PaperItemDtoListOutput {
    public DataFetcher<Iterable<Object>> entries();
  }
  
  public interface ConvertToMarkdownForWebsiteResponseDataTypeOutput {
    public DataFetcher<String> anonymous2();
  }
  
  public interface Query {
    public DataFetcher<Object> lookupPapersFromAuthor();
  }
  
  public interface Mutation {
    public DataFetcher<Object> createPaperItem();
    public DataFetcher<Object> convertToMarkdownForWebsite();
  }
  
}
