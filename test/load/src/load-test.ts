import {Options} from 'k6/options';
import {setupUsers, distributeVUs, User} from './utils.ts';
import runAuth from './flows/auth.ts';
import runArticles from './flows/articles.ts';
import runProfiles from './flows/profiles.ts';
import runComments from './flows/comments.ts';
import runFullFlow from './flows/full-flow.ts';
import runRegister from './flows/register.ts';

const TOTAL_VUS = parseInt(__ENV.VUS || '10');
const DURATION = __ENV.DURATION || '30s';

const weights = {
    register: 0.1,
    auth: 0.2,
    articles: 0.3,
    profiles: 0.15,
    comments: 0.15,
    fullFlow: 0.1,
};

const distribution = distributeVUs(TOTAL_VUS, weights);

export let options: Options = {
    scenarios: {
        register: {
            executor: 'constant-vus',
            vus: distribution.register,
            duration: DURATION,
            exec: 'registerScenario',
        },
        auth: {
            executor: 'constant-vus',
            vus: distribution.auth,
            duration: DURATION,
            exec: 'authScenario',
        },
        articles: {
            executor: 'constant-vus',
            vus: distribution.articles,
            duration: DURATION,
            exec: 'articlesScenario',
        },
        profiles: {
            executor: 'constant-vus',
            vus: distribution.profiles,
            duration: DURATION,
            exec: 'profilesScenario',
        },
        comments: {
            executor: 'constant-vus',
            vus: distribution.comments,
            duration: DURATION,
            exec: 'commentsScenario',
        },
        fullFlow: {
            executor: 'constant-vus',
            vus: distribution.fullFlow,
            duration: DURATION,
            exec: 'fullFlowScenario',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<100000'],
        'http_req_duration{name:Register}': ['p(95)<100000'],
        'http_req_duration{name:Login}': ['p(95)<100000'],
        'http_req_duration{name:GetCurrentUser}': ['p(95)<100000'],
        'http_req_duration{name:CreateArticle}': ['p(95)<100000'],
        'http_req_duration{name:GetArticle}': ['p(95)<100000'],
        'http_req_duration{name:AddComment}': ['p(95)<100000'],
        'http_req_duration{name:GetComments}': ['p(95)<100000'],
        'http_req_duration{name:FavoriteArticle}': ['p(95)<100000'],
        'http_req_duration{name:DeleteComment}': ['p(95)<100000'],
        'http_req_duration{name:UnfavoriteArticle}': ['p(95)<100000'],
        'http_req_duration{name:DeleteArticle}': ['p(95)<100000'],
        'http_req_duration{name:GetTags}': ['p(95)<100000'],
        'http_req_duration{name:GetGlobalArticles}': ['p(95)<100000'],
        'http_req_duration{name:GetArticlesFeed}': ['p(95)<100000'],
        'http_req_duration{name:FollowUser}': ['p(95)<100000'],
        'http_req_duration{name:UnfollowUser}': ['p(95)<100000'],
        'http_req_duration{name:GetProfile}': ['p(95)<100000'],
    },
};

export function setup() {
    return setupUsers(10);
}

export function registerScenario() {
    runRegister();
}

export function authScenario(users: User[]) {
    runAuth(users);
}

export function articlesScenario(users: User[]) {
    runArticles(users);
}

export function profilesScenario(users: User[]) {
    runProfiles(users);
}

export function commentsScenario(users: User[]) {
    runComments(users);
}

export function fullFlowScenario(users: User[]) {
    runFullFlow(users);
}

export default function (users: User[]) {
    runFullFlow(users);
}
