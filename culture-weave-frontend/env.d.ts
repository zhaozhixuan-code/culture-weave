/// <reference types="vite/client" />
/// <reference path="./src/api/typings.d.ts" />

declare module '*.vue' {
    import type {DefineComponent} from 'vue'
    const component: DefineComponent<{}, {}, any>
    export default component
}
