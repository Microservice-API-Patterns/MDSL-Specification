const { ApolloServer, gql } = require('apollo-server');

// A schema is a collection of type definitions (hence "typeDefs")
// that together define the "shape" of queries that are executed against
// your data.
const typeDefs = gql`
# GraphQL generated for endpoint PaperArchiveFacade of ReferenceManagementServiceAPI


input PaperItemDTOInput {
	title: String!
	authors: String!
	venue: String!
	paperItemId: PaperItemKeyInput!
}
input PaperItemKeyInput {
	doi: String!
}
input CreatePaperInput {
	who: String!
	what: String!
	where: String!
}
input lookupPapersFromAuthorRequestDataTypeInput {
	anonymous1: String!
}
input PaperItemDTOListInput {
	entries: [PaperItemDTOInput!]!
}
input ConvertToMarkdownForWebsiteResponseDataTypeInput {
	anonymous2: String!
}
type PaperItemDTOOutput {
	title: String!
	authors: String!
	venue: String!
	paperItemId: PaperItemKeyOutput!
}
type PaperItemKeyOutput {
	doi: String!
}
type CreatePaperOutput {
	who: String!
	what: String!
	where: String!
}
type lookupPapersFromAuthorRequestDataTypeOutput {
	anonymous1: String!
}
type PaperItemDTOListOutput {
	entries: [PaperItemDTOOutput!]!
}
type ConvertToMarkdownForWebsiteResponseDataTypeOutput {
	anonymous2: String!
}

type Query {
	lookupPapersFromAuthor(
		anonymousInput: lookupPapersFromAuthorRequestDataTypeInput
	): PaperItemDTOListOutput
}

type Mutation {
	createPaperItem(
		anonymousInput: CreatePaperInput
	): PaperItemDTOOutput
	convertToMarkdownForWebsite(
		anonymousInput: PaperItemKeyInput
	): ConvertToMarkdownForWebsiteResponseDataTypeOutput
}

schema {
	query: Query
	mutation: Mutation
}

# additional scalars for types in MDSL
scalar Raw
scalar VoidResponse
`;

const papers = {
  entries: [
    {
      title: 'Domain-driven Service Design - Context Modeling, Model Refactoring and Contract Generation',
      authors: 'Stefan Kapferer and Olaf Zimmermann',
      venue: 'OST'
    },
    {
      title: 'Dimensions of Successful Web API Design and Evolution: Context, Contracts, Components',
      authors: 'Olaf Zimmermann',
      venue: 'OST'
    },
  ]
};

const resolvers = {
  Query: {
    lookupPapersFromAuthor: () => papers,
  },
  Mutation: {
	createPaperItem: ( parent, { anonymousInput: { who, what, where }} ) => {
		const returnObj = {
			title: what,
			authors: who,
			venue: where,
			paperItemId: {
				doi: "Fake-DOI"
			}
		}
		return returnObj;
	}
  }
};

// The ApolloServer constructor requires two parameters: your schema
// definition and your set of resolvers.
const server = new ApolloServer({ typeDefs, resolvers });

// The `listen` method launches a web server.
server.listen().then(({ url }) => {
  console.log(`🚀  Server ready at ${url}`);
});

