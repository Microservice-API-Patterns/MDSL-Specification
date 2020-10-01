import React from "react";
import { render } from "react-dom";
import {
  ApolloClient,
  InMemoryCache,
  ApolloProvider,
  useQuery,
  gql
} from "@apollo/client";

const client = new ApolloClient({
	uri: 'http://localhost:4000',
  cache: new InMemoryCache()
});

function PaperItems() {
  const { loading, error, data } = useQuery(gql`
    query {
      lookupPapersFromAuthor(anonymousInput: {anonymous1: "Olaf Zimmermann"}) {
        entries {
          title
          authors
          venue
        }
      }
    }`);

  if (loading) return <p>Loading...</p>;
  if (error) return <p>Error :(</p>;

  return data.lookupPapersFromAuthor.entries.map(({ title, authors, venue }) => (
    <div key={title}>
      <p>
	  {authors}: {title} ({venue})
      </p>
    </div>
  ));
}

function App() {
  return (
    <ApolloProvider client={client}>
      <div>
        <h2>My first Apollo app 🚀</h2>
        <PaperItems />
      </div>
    </ApolloProvider>
  );
}

render(<App />, document.getElementById("root"));

