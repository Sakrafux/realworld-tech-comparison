import http from "k6/http";
import { sleep } from "k6";

export const options = {
    scenarios: {
        home: {
            executor: "constant-vus",
            vus: 1000,
            duration: "30s",
            exec: "homeScenario",
        },
        profile: {
            executor: "constant-vus",
            vus: 500,
            duration: "30s",
            exec: "profileScenario",
        },
        article: {
            executor: "constant-vus",
            vus: 500,
            duration: "30s",
            exec: "articleScenario",
        },
    },
    thresholds: {
        http_req_duration: ["p(95)<100000"],
        "http_req_duration{scenario:home}": ["p(95)<100000"],
        "http_req_duration{scenario:profile}": ["p(95)<100000"],
        "http_req_duration{scenario:article}": ["p(95)<100000"],
    },
};

export function homeScenario() {
    http.get("http://localhost:3000/");
    sleep(1);
}

export function profileScenario() {
    http.get("http://localhost:3000/profile/asdf");
    sleep(1);
}

export function articleScenario() {
    http.get("http://localhost:3000/article/asdfasdf");
    sleep(1);
}
