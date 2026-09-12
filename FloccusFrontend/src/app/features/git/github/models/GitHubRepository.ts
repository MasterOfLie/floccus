export interface GitHubRepository {
    id: number;
    name: string;
    fullName: string;
    htmlUrl: string;
    defaultBranch: string;
    privateRepository: boolean;
}